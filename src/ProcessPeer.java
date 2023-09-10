import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class ProcessPeer
{
    public static final String Peer_path = "PeerInfo.cfg";
    public static final String Config_Path = "CommonConfig.cfg";
    public static HashMap<String,PeerRemote> PeerHashMap=new HashMap<>();
    public  static HashMap<String,PeerRemote> NeighbourHashMap=new HashMap<>();
    static  LogData logs;
    public static volatile Timer timer1;
    public static volatile Timer timer2;
    public static PayLoad PayloadCurrent=null;
    static int clientPort;
    public static ServerSocket socket=null;
    public static Thread thread;
    public static String peerId;
    public static boolean Flag = false;
    public static Queue<Parameters> queue = new LinkedList<>();
    public static Vector<Thread> socket_thread=new Vector<>();
    public static Thread mp;
    public  static Vector<Thread> peer_thread=new Vector<>();
    public static HashMap<String,Socket> peerData=new HashMap<>();
    public static volatile Hashtable<String, PeerRemote> preferredNeighboursHashMapTable = new Hashtable<>();
    public static volatile Hashtable<String, PeerRemote> NeighborHashMap_Unchocked = new Hashtable<>();
    public static void main(String[] args) throws Exception
    {

        peerId=args[0];
        // peerId = "1001";
        logs =new LogData("P"+peerId+".log");
        boolean flag=false;
        try
        {

            logs.showLog(peerId+" start");
            Scanner myReader= null;
            String configs;
            BufferedReader Read = null;

            //Reading the Values from Commonconfig.cfg file to attain the data like Number of Neighbours, Unchoking Interval, 
            //                          Optimistic Unchoking Interval, File Name, File Size, Piece Size
            try
            {
                File myObj = new File(Config_Path);
                myReader = new Scanner(myObj);
                while (myReader.hasNextLine())
                {
                    String data = myReader.nextLine();
                    String[] line=data.split(" ");
                    if(line[0].trim().equals("NumberOfPreferredNeighbors"))
                    {
                        Payloadpiece.numberOfPreferredNeighbors=Integer.parseInt(line[1]);
                    }
                    if(line[0].trim().equals("UnchokingInterval"))
                    {
                        Payloadpiece.unchokingInterval=Integer.parseInt(line[1]);
                    }
                    if(line[0].trim().equals("OptimisticUnchokingInterval"))
                    {
                        Payloadpiece.optimisticUnchokingInterval=Integer.parseInt(line[1]);
                    }
                    if(line[0].trim().equals("FileName"))
                    {
                        Payloadpiece.fileName=line[1];
                    }
                    if(line[0].trim().equals("FileSize"))
                    {
                        Payloadpiece.fileSize=Integer.parseInt(line[1]);
                    }
                    if(line[0].trim().equals("PieceSize"))
                    {
                        Payloadpiece.pieceSize=Integer.parseInt(line[1]);
                    
                    }
                }

                //Values of Peers and Peer Host Data to start remote servers with the obtained data
                Read=new BufferedReader(new FileReader(Peer_path));
                while((configs=Read.readLine())!=null)
                {
                    String[] line=configs.split(" ");
                    PeerHashMap.put(line[0],new PeerRemote(line[0],line[1],line[2],line[3].equals("1")));
            
                }
            }
            catch (Exception ex1)
            {
               logs.showLog(ex1.getMessage());
            }
            finally
            {
                myReader.close();
                Read.close();
            }

            //Traversing through tht obatined peer hashmap to make add them with their respective neighbour peer hashmap
            for(Map.Entry<String,PeerRemote> hm1: PeerHashMap.entrySet() )
            {
                if (!hm1.getKey().equals(peerId)) {
                    NeighbourHashMap.put(hm1.getKey(), hm1.getValue());
                }
            }

            //Traversing through all the Remote Peer using string added in Hashmap to check about the file status
            x:for(Map.Entry<String,PeerRemote> mp: PeerHashMap.entrySet() )
            {
                PeerRemote r = mp.getValue();
                if (r.peerId.equals(peerId))
                {
                    clientPort = Integer.parseInt(r.peerPort);

                    //Checking whether the remote server conatins the file
                    if (r.hasFile)
                    {
                      flag=true;
                      break x;
                    }
                }
            }

            //creating Thread to create sockets for the respective ports to pass file
            PayloadCurrent=new PayLoad();
            PayloadCurrent.initPayLoad(peerId,flag);
            Thread PeerThread=new Thread(new DataController(peerId));
            PeerThread.start();
            // Falg counter to check the data file available in all peers to close the created sockets and start the threads
            if(flag)
            {
                try
                {
                    ProcessPeer.socket = new ServerSocket(clientPort);
                    thread = new Thread(new Server(ProcessPeer.socket, peerId));
                    thread.start();
                }
                catch (Exception ex)
                {
                   logs.showLog(peerId+ " thread exception");
                    logs.closeLog();
                    System.exit(0);
                }
            }
            else
            {
                //Passing the files to peers from bits and make connections with peers from connecting them accourding to peerhashmap
                PeerFile();
                for(Map.Entry<String,PeerRemote> hm: PeerHashMap.entrySet() )
                {
                    PeerRemote remotePeerInfo=hm.getValue();
                    if(Integer.parseInt(peerId)>Integer.parseInt(hm.getKey()))
                    {
                        PeerController p=new PeerController(remotePeerInfo.getPeerAddress(),Integer.parseInt(remotePeerInfo.getPeerPort()),1, peerId);
                        Thread temp=new Thread(p);
                        peer_thread.add(temp);
                        temp.start();
                    }

                }
                try
                {
                    ProcessPeer.socket = new ServerSocket(clientPort);
                    thread = new Thread(new Server(ProcessPeer.socket, peerId));
                    thread.start();
                }
                catch (Exception ex)
                {
                    logs.showLog(peerId+ " thread exception");
                    logs.closeLog();
                    System.exit(0);
                }
            }

            // timers to check the interval for the respective Unchoking and OptimisticChocking Intervals
            timer1 = new Timer();
            timer1.schedule(new PreferNeighbours(),0,Payloadpiece.unchokingInterval * 1000L);
            timer2 = new Timer();
            timer2.schedule(new UnChoockedNeighbor(),0,Payloadpiece.optimisticUnchokingInterval * 1000L);
            Thread cThread=thread;
            Thread mp=PeerThread;
            while(true) {
                //Checking the file is present in all the remote servers and terminating the respective threads
                Flag = ProcessDone();
                if (Flag) {
                    logs.showLog("Process Completed");

                    timer1.cancel();
                    timer2.cancel();

                    try {
                        Thread.currentThread();
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {
                    }

                    if (cThread.isAlive())
                       cThread.interrupt();

                    if (mp.isAlive())
                        mp.interrupt();

                    for (Thread thread : peer_thread)
                        if (thread.isAlive())
                            thread.interrupt();

                    for (Thread thread : socket_thread)
                        if (thread.isAlive())
                            thread.interrupt();

                    break;
                } else {
                    try {
                        Thread.currentThread();
                        Thread.sleep(5000);
                    } catch (InterruptedException ignored) {}
                }
            }
        }
        catch(Exception exception) {
            logs.showLog(String.format(peerId+" error : "+ exception.getMessage()));
        }
        finally {
            logs.showLog(String.format(peerId+" is Ended"));
            logs.closeLog();
            System.exit(0);
        }
    }

    //passing the file from peer to peer using the neighboring peers maintained in the hashmap
    private static void PeerFile()
    {
        try
        {
            //Initially creates dummy files and then starts transferring data pieces from peer to peer
            byte intialByte=0;
            int i=0;
            File f=new File(peerId,Payloadpiece.fileName);
            OutputStream FileOutput=new FileOutputStream(f,true);
            while(i<Payloadpiece.fileSize)
            {
                FileOutput.write(intialByte);
                i++;
            }
            FileOutput.close();

        }
        catch (Exception e)
        {
            logs.showLog("Dummy file creation error "+peerId);
        }
    }

    //Data passing is done in queue  and the that sent file is removed from the queue
    public static synchronized Parameters removeDataFromQueue(){
        Parameters dp = null;
        if(queue.isEmpty()){}
        else {
            dp = queue.remove();
        }
        return dp;
    }

    //basic queue addition function
    public static synchronized void addToQueue(Parameters dp)
    {
        queue.add(dp);
    }
    
    //Unchoke reequest condition function
    static void sendRequestToUnchoke( String remotePeerID,Socket socket) {
        logs.showLog(peerId+" UNCHOKE message to "+ remotePeerID);
        
        Output_Reader( MessageData.DataToByte(new MessageData(PayLoad.unChoke)),socket);
    }
    // Have message passing function between peer to peer
    static void sendHaveMessage( String remotePeerID,Socket socket) {
        byte[] b = ProcessPeer.PayloadCurrent.encodeData();
        logs.showLog(peerId+" HAVE message to "+ remotePeerID);
        
        Output_Reader( MessageData.DataToByte(new MessageData(PayLoad.have, b)),socket);
    }

    //Check the data from and at nect peer and maintain the variables iscompleted, isinterested 
    //  and ischocked for the conditional criteria to pass the data between the peers
    public static void readNextPeerData() {
        Scanner myReader= null;
        try {
            File myObj = new File(Peer_path);
                myReader = new Scanner(myObj);
                while (myReader.hasNextLine()){
                    String data = myReader.nextLine();
                    String[]p = data.trim().split(" ");
                    String peerID = p[0];
                    if(Integer.parseInt(p[3]) == 1) {
                        PeerHashMap.get(peerID).isCompleted = 1;
                        PeerHashMap.get(peerID).isInterested = 0;
                        PeerHashMap.get(peerID).isChoked = 0;
                    }
            }
            myReader.close();
        }
        catch (Exception exception) {
            logs.showLog(peerId + "" +exception.toString());
        }
    }

    //Chcek the data from peerpath config to get the local host and file presence data and then update the data
    public static synchronized boolean ProcessDone() {
        int Count = 1;
        Scanner myReader= null;
        try {
            File myObj = new File(Peer_path);
            myReader = new Scanner(myObj);
            while (myReader.hasNextLine()){ 
                String data = myReader.nextLine();
                Count = Count
                        * Integer.parseInt(data.trim().split(" ")[3]);
            }
            myReader.close();
            return Count != 0;
        } catch (Exception e) {
            logs.showLog(e.toString());
            return false;
        }
    }

    //Output stream writer function to write the log output data
    private static void Output_Reader(byte[] b,Socket socket) {
        try {
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(b);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}

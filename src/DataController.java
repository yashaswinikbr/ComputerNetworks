import java.io.*;
import java.net.Socket;
import java.util.*;


public class DataController implements Runnable
{
    private static String pId = null;
    RandomAccessFile rf;

    public DataController(String pId) {
        DataController.pId = pId;
    }
    
    //Datacollector class that functions on Collecting data from file and checks various condtions of Choke,
    //  Unchoke, Have, Bitfield , Send, Recieve, Interested and Not Interested messaged between peers
    // The cases funtions for various messages to switch with respect to the message obtained from the neighboring peer
    public void run() {
        String dataType;
        String currentPeerId;
        MessageData message;
        Parameters param;
        
        while(true)
        {
            param  = ProcessPeer.removeDataFromQueue();
            while(param == null) {
                Thread.currentThread();
                try {
                    Thread.sleep(500);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                param  = ProcessPeer.removeDataFromQueue();
            }

            message = param.getM();

            dataType = message.getDataType();
            currentPeerId = param.getpId();
            int state = ProcessPeer.PeerHashMap.get(currentPeerId).state;
            if(dataType.equals(""+PayLoad.have) && state != 14)
            {
                ProcessPeer.logs.showLog(ProcessPeer.peerId+" HAVE message from "+ currentPeerId);
                if(comparePayLoadData(currentPeerId,message)) {
                    InterestedMsg(currentPeerId,ProcessPeer.peerData.get(currentPeerId));
                    ProcessPeer.PeerHashMap.get(currentPeerId).state = 9;
                }
                else {
                    NotInterestedMsg(currentPeerId,ProcessPeer.peerData.get(currentPeerId));
                    ProcessPeer.PeerHashMap.get(currentPeerId).state = 13;
                }
            }
            else {
                switch (state)
                {
                    case 2:
                        if (dataType.equals(""+PayLoad.bitField)) {
                            ProcessPeer.logs.showLog(ProcessPeer.peerId+" BITFIELD message from "+ currentPeerId);
                            BitFieldMsg(currentPeerId,ProcessPeer.peerData.get(currentPeerId));
                            ProcessPeer.PeerHashMap.get(currentPeerId).state = 3;
                        }
                        break;

                    case 3:
                        
                         if (dataType.equals(""+PayLoad.intersted)) {
                            ProcessPeer.logs.showLog(ProcessPeer.peerId+" REQUEST message to "+ currentPeerId);
                            ProcessPeer.logs.showLog( ProcessPeer.peerId+" INTERESTED message from "+currentPeerId);
                            ProcessPeer.PeerHashMap.get(currentPeerId).isInterested = 1;
                            ProcessPeer.PeerHashMap.get(currentPeerId).isHandShake = 1;

                            if(!ProcessPeer.preferredNeighboursHashMapTable.containsKey(currentPeerId) && !ProcessPeer.NeighborHashMap_Unchocked.containsKey(currentPeerId)) {
                                ChokeMsg(currentPeerId,ProcessPeer.peerData.get(currentPeerId));
                                ProcessPeer.PeerHashMap.get(currentPeerId).isChoked = 1;
                                ProcessPeer.PeerHashMap.get(currentPeerId).state  = 6;
                            }
                            else {
                                ProcessPeer.PeerHashMap.get(currentPeerId).isChoked = 0;
                                UnChokeMsg(currentPeerId,ProcessPeer.peerData.get(currentPeerId));
                                ProcessPeer.PeerHashMap.get(currentPeerId).state = 4 ;
                            }
                        }
                        else if (dataType.equals(""+PayLoad.notInterested)) {
                            ProcessPeer.logs.showLog( ProcessPeer.peerId+" NOT INTERESTED message from "+currentPeerId);
                            ProcessPeer.PeerHashMap.get(currentPeerId).isInterested = 0;
                            ProcessPeer.PeerHashMap.get(currentPeerId).state = 5;
                            ProcessPeer.PeerHashMap.get(currentPeerId).isHandShake = 1;
                        }
                        break;

                    case 4:
                        if (dataType.equals(""+PayLoad.request)) {
                            Transfer(ProcessPeer.peerData.get(currentPeerId), message, currentPeerId);
                            if(!ProcessPeer.preferredNeighboursHashMapTable.containsKey(currentPeerId) && !ProcessPeer.NeighborHashMap_Unchocked.containsKey(currentPeerId)) {
                                ChokeMsg(currentPeerId,ProcessPeer.peerData.get(currentPeerId));
                                ProcessPeer.PeerHashMap.get(currentPeerId).isChoked = 1;
                                ProcessPeer.PeerHashMap.get(currentPeerId).state = 6;
                            }
                        }
                        break;

                    case 8:
                        if (dataType.equals(""+PayLoad.bitField)) {
                            if(comparePayLoadData(currentPeerId,message)) {
                                InterestedMsg(currentPeerId,ProcessPeer.peerData.get(currentPeerId));
                                ProcessPeer.PeerHashMap.get(currentPeerId).state = 9;
                            }
                            else {
                                NotInterestedMsg(currentPeerId,ProcessPeer.peerData.get(currentPeerId));
                                ProcessPeer.PeerHashMap.get(currentPeerId).state = 13;
                            }
                        }
                        break;

                    case 9:
                        if (dataType.equals(""+PayLoad.choke)) {
                            ProcessPeer.logs.showLog( ProcessPeer.peerId+" CHOKED message by "+currentPeerId);
                            ProcessPeer.PeerHashMap.get(currentPeerId).state = 14;
                        }
                        else if (dataType.equals(""+PayLoad.unChoke)) {
                            ProcessPeer.logs.showLog( ProcessPeer.peerId+" CHOKED message by "+currentPeerId);
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            ProcessPeer.logs.showLog( ProcessPeer.peerId+" UNCHOKED message by "+currentPeerId);
                            int initialMismatch = ProcessPeer.PayloadCurrent.fetchFirstBitField(
                                    ProcessPeer.PeerHashMap.get(currentPeerId).payloadData);
                            if(initialMismatch != -1) {
                                sendRequest(initialMismatch,ProcessPeer.peerData.get(currentPeerId));
                                ProcessPeer.PeerHashMap.get(currentPeerId).state = 11;
                                ProcessPeer.PeerHashMap.get(currentPeerId).sTime = new Date();
                            }
                            else
                                ProcessPeer.PeerHashMap.get(currentPeerId).state = 13;
                        }
                        break;

                    case 11:
                        if (dataType.equals(""+PayLoad.choke)) {
                        ProcessPeer.logs.showLog( ProcessPeer.peerId+" CHOKED "+currentPeerId);
                        ProcessPeer.PeerHashMap.get(currentPeerId).state = 14;
                        }
                        
                        else if (dataType.equals(""+PayLoad.piece)) {
                            byte[] payloadArray = message.getPayLoadArray();
                            ProcessPeer.PeerHashMap.get(currentPeerId).fTime = new Date();
                            long d = ProcessPeer.PeerHashMap.get(currentPeerId).fTime.getTime() - ProcessPeer.PeerHashMap.get(currentPeerId).sTime.getTime() ;
                            ProcessPeer.PeerHashMap.get(currentPeerId).streamRate= ((double)(payloadArray.length + PayLoad.sizeOfMessage + PayLoad.typeOfMessage) / (double)d) * 100;
                            int a=PayLoad.maxPieceLength;
                            Payloadpiece p1=new Payloadpiece();
                            byte[] b=new byte[a];
                            System.arraycopy(payloadArray,0,b,0,a);
                            p1.pindx=PayLoad.ByteArray_to_Int(b,0);
                            p1.piece=new byte[payloadArray.length-a];
                            System.arraycopy(payloadArray,a,p1.piece,0,payloadArray.length-a);
                            Payloadpiece p = p1;
                            ProcessPeer.PayloadCurrent.updatePayLoad(p,""+currentPeerId);
                            int indx = ProcessPeer.PayloadCurrent.fetchFirstBitField(
                                    ProcessPeer.PeerHashMap.get(currentPeerId).payloadData);
                            if(indx != -1) {
                                sendRequest(indx,ProcessPeer.peerData.get(currentPeerId));
                                ProcessPeer.PeerHashMap.get(currentPeerId).state  = 11;
                                ProcessPeer.PeerHashMap.get(currentPeerId).sTime = new Date();
                            }
                            else
                                ProcessPeer.PeerHashMap.get(currentPeerId).state = 13;
                            ProcessPeer.readNextPeerData();;

                            Enumeration<String> keys = Collections.enumeration(ProcessPeer.PeerHashMap.keySet());
                            while(keys.hasMoreElements())
                            {
                                String nextElement = keys.nextElement();
                                PeerRemote r = ProcessPeer.PeerHashMap.get(nextElement);
                                if(nextElement.equals(ProcessPeer.peerId))continue;
                                if (r.isCompleted == 0 && r.isChoked == 0 && r.isHandShake == 1) {
                                    HaveMsg(nextElement,ProcessPeer.peerData.get(nextElement));
                                    ProcessPeer.PeerHashMap.get(nextElement).state = 3;
                                }
                            }
                        }
                         
                        break;

                    case 14:
                        if (dataType.equals(""+PayLoad.unChoke)) {
                            ProcessPeer.logs.showLog( ProcessPeer.peerId+" CHOKED "+currentPeerId);
                            try {
                            Thread.sleep(6000);
                            } catch (Exception e) {
                           System.out.println(e.getMessage());;
                            }
                            ProcessPeer.logs.showLog( ProcessPeer.peerId+" UNCHOKED "+currentPeerId);
                            ProcessPeer.PeerHashMap.get(currentPeerId).state = 14;
                        }
                        else if (dataType.equals(""+PayLoad.have)) {
                            if(comparePayLoadData(currentPeerId,message)) {
                                InterestedMsg(currentPeerId,ProcessPeer.peerData.get(currentPeerId));
                                ProcessPeer.PeerHashMap.get(currentPeerId).state = 9;
                            }
                            else {
                                NotInterestedMsg(currentPeerId,ProcessPeer.peerData.get(currentPeerId) );
                                ProcessPeer.PeerHashMap.get(currentPeerId).state = 13;
                            }
                        }
                        
                        break;
                }
            }

        }
    }

    // SEND request function  with particular scoket
    private void sendRequest( int pNo,Socket socket) {
        byte[] p = new byte[PayLoad.maxPieceLength];
        for (int i = 0; i < PayLoad.maxPieceLength; i++)
            p[i] = 0;

        byte[] pindxArray = PayLoad.Int_to_Byte(pNo);
        System.arraycopy(pindxArray, 0, p, 0,
                pindxArray.length);
       
        sendOutput(MessageData.DataToByte(new MessageData(PayLoad.request, p)),socket);
    }
   
    // NOT INTRESTED function senda the promp to particular PID and socket
    private void NotInterestedMsg(String pId,Socket socket ) {
        ProcessPeer.logs.showLog(ProcessPeer.peerId+" NOT INTERESTED to "+ pId);
               sendOutput(MessageData.DataToByte(new MessageData(PayLoad.notInterested)),socket);
    }

    // INTRESTED function senda the promp to particular PID and socket
    private void InterestedMsg(String pId,Socket socket) {
        ProcessPeer.logs.showLog(ProcessPeer.peerId+" REQUEST to "+ pId);
        ProcessPeer.logs.showLog(ProcessPeer.peerId+" INTERESTED to "+ pId);
        sendOutput(MessageData.DataToByte(new MessageData(PayLoad.intersted)),socket);
    }

    //Compares payload data with current present data and the total payload data
    private boolean comparePayLoadData( String pId,MessageData md) {
        PayLoad payloadData = PayLoad.decodeData(md.getPayLoadArray());
        ProcessPeer.PeerHashMap.get(pId).payloadData = payloadData;
        return ProcessPeer.PayloadCurrent.comparePayLoadData(payloadData);
    }

    //UNCHOKE Message function with the respective Socket and PeerID
    private void UnChokeMsg( String pId,Socket socket) {
        ProcessPeer.logs.showLog(ProcessPeer.peerId+" UNCHOKE to "+ pId);
        sendOutput(MessageData.DataToByte(new MessageData(PayLoad.unChoke)),socket);
    }

    //CHOKE Message function with the respective Socket and PeerID
    private void ChokeMsg(String pId,Socket socket) {
        ProcessPeer.logs.showLog(ProcessPeer.peerId+" CHOKE to "+ pId);
        sendOutput(MessageData.DataToByte(new MessageData(PayLoad.choke)),socket);
    }

    //Send output strean data for the byte array of the encode Bitfield
    private void sendOutput(byte[] encodedBitField,Socket socket ) {
        try {
            OutputStream op = socket.getOutputStream();
            op.write(encodedBitField);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());;
        }
    }

    //HAVE Message function with the respective Socket and PeerID
    private void HaveMsg(String pId,Socket socket)  {
        ProcessPeer.logs.showLog(ProcessPeer.peerId+" HAVE to "+ pId);
        sendOutput( MessageData.DataToByte(new MessageData(PayLoad.have, ProcessPeer.PayloadCurrent.encodeData())),socket);
    }

    //Transfer data request based on the request message, scoket and Peer ID
    private void Transfer(Socket socket, MessageData requestMessage, String pId)
    {
        byte[] bindx = requestMessage.getPayLoadArray();
        int pindx = PayLoad.ByteArray_to_Int(bindx,0);
        byte[] byteRead = new byte[Payloadpiece.pieceSize];
        int readBytes = 0;
        File f = new File(ProcessPeer.peerId, Payloadpiece.fileName);

        ProcessPeer.logs.showLog(ProcessPeer.peerId+" sent PIECE "+pindx+" to "+ pId);
        try {
            rf = new RandomAccessFile(f,"r");
            rf.seek((long) pindx *Payloadpiece.pieceSize);
            readBytes = rf.read(byteRead, 0, Payloadpiece.pieceSize);
        }
        catch (Exception ex) {
            ProcessPeer.logs.showLog(ProcessPeer.peerId+" error reading: "+ex.toString());
        }

        byte[] buffbytes = new byte[readBytes + PayLoad.maxPieceLength];
        System.arraycopy(bindx, 0, buffbytes, 0, PayLoad.maxPieceLength);
        System.arraycopy(byteRead, 0, buffbytes, PayLoad.maxPieceLength, readBytes);

        sendOutput(MessageData.DataToByte(new MessageData(PayLoad.piece, buffbytes)), socket);
        try{rf.close();}
        catch(Exception ignored){}
    }
    
    //BITFIELD message function with respective PeerID and Socket
    private void BitFieldMsg(String pId,Socket socket) {
        ProcessPeer.logs.showLog(ProcessPeer.peerId+" BITFIELD to "+ pId);
        sendOutput(MessageData.DataToByte(new MessageData(+PayLoad.bitField, ProcessPeer.PayloadCurrent.encodeData())),socket);
    }


}

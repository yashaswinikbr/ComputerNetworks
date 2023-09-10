import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class PeerController implements Runnable
{
    private InputStream ip;
    private OutputStream op;
    public static final int noofActiveSessions=1;
    private Socket socket=null;
    private int sessionType;
    String remotepId;
    String currentpId;
    public void run()
    {
        Parameters dp=new Parameters();
        byte[] messageHandShakeArray=new byte[32];
        byte[] bufferedMessage=new byte[PayLoad.sizeOfMessage+PayLoad.typeOfMessage];
        byte[] dataLength;
        byte[] dataType;
        try
        {
            if(this.sessionType !=  noofActiveSessions)
            {
                updatePeersData(messageHandShakeArray);
                if(connectWithPeer())
                {
                    throw  new Exception("Failed connecting : "+this.currentpId);
                }
                ProcessPeer.logs.showLog(this.currentpId+" Handshaking with "+ this.remotepId);                
                ProcessPeer.logs.showLog(this.currentpId+" sent TCP with "+this.remotepId);
                ProcessPeer.PeerHashMap.get(remotepId).state=2;

            }
            else
            {
                if(connectWithPeer())
                {
                    throw  new Exception("Failed connecting : "+this.currentpId);
                }
                updatePeersData(messageHandShakeArray);
                ProcessPeer.logs.showLog(this.currentpId+" Handshaking with "+ this.remotepId);
                ProcessPeer.PeerHashMap.get(remotepId).state=8;
                MessageData md=new MessageData(PayLoad.bitField, ProcessPeer.PayloadCurrent.encodeData());
                op.write(MessageData.DataToByte(md));
            }
            x:while (true)
            {
                int hBytes;
                if((hBytes=ip.read(bufferedMessage))==-1)
                {
                    break x;
                }
                dataLength=new byte[PayLoad.sizeOfMessage];
                dataType=new byte[PayLoad.typeOfMessage];
                System.arraycopy(bufferedMessage,0,dataLength,0,PayLoad.sizeOfMessage);
                System.arraycopy(bufferedMessage,PayLoad.sizeOfMessage,dataType,0,PayLoad.typeOfMessage);
                MessageData md=new MessageData();
                md.setDataLength(dataLength);
                md.setDataType(dataType);
                String s="0 1 2 3";
                if(s.contains(md.getDataType()))
                {
                  dp.m=md;
                }
                else
                {
                    int readBytes=0;
                    int bytesToRead;
                    byte[] payloadMessage=new byte[md.getLengthOfMessage()-1];
                    while (readBytes<md.getLengthOfMessage()-1)
                    {
                        bytesToRead=ip.read(payloadMessage,readBytes,md.getLengthOfMessage()-1-readBytes);
                        if(bytesToRead==-1)
                        {
                            return ;
                        }
                        readBytes+=bytesToRead;
                    }
                    byte[] messageDataPayLoad=new byte[md.getLengthOfMessage()+PayLoad.sizeOfMessage];
                    System.arraycopy(bufferedMessage,0,messageDataPayLoad,0,PayLoad.sizeOfMessage+PayLoad.typeOfMessage);
                    System.arraycopy(payloadMessage,0,messageDataPayLoad,PayLoad.sizeOfMessage+PayLoad.typeOfMessage,payloadMessage.length);
                    MessageData m=new MessageData();
                    byte[] dlen=new byte[PayLoad.sizeOfMessage];
                    byte[] dType=new byte[PayLoad.typeOfMessage];
                    byte[] payload;
                    int mlen;
                    try
                    {
                        if(messageDataPayLoad.length<PayLoad.sizeOfMessage+PayLoad.typeOfMessage || messageDataPayLoad==null)
                        {
                            throw new Exception("Message is Not Valid");
                        }
                        System.arraycopy(messageDataPayLoad,0,dlen,0,PayLoad.sizeOfMessage);
                        System.arraycopy(messageDataPayLoad,PayLoad.sizeOfMessage,dType,0,PayLoad.typeOfMessage);
                        m.setDataLength(dlen);
                        m.setDataType(dType);
                        mlen=PayLoad.ByteArray_to_Int(dlen,0);
                        if(mlen>1)
                        {
                            payload=new byte[mlen-1];
                            System.arraycopy(messageDataPayLoad,PayLoad.sizeOfMessage+PayLoad.typeOfMessage,payload,0,messageDataPayLoad.length-PayLoad.sizeOfMessage-PayLoad.typeOfMessage);
                            m.setPayLoadArray(payload);
                        }
                    }
                    catch (Exception ex) {
                        System.out.println(ex.getMessage());
                    
                    }
                    dp.m=m;
                }
                dp.pId=this.remotepId;
                ProcessPeer.addToQueue(dp);
            }
        }
        catch (Exception ex)
        {
            ProcessPeer.logs.showLog(ex.getMessage());
        }

    }
    public void updatePeersData(byte[] hArray) throws IOException {
        x:while(1==1)
        {
            ip.read(hArray);
            String s = new String(hArray, StandardCharsets.UTF_8);
            // Handshake h = new Handshake.byteToHandShake(hArray);
            if(s.substring(0, 18).equals(Payloadpiece.handshakeHeader))
            {
                remotepId=s.substring(s.length()-4, s.length());
                ProcessPeer.logs.showLog(this.currentpId+" got handshake message from "+this.remotepId);
                ProcessPeer.peerData.put(this.remotepId,this.socket);
                break x;
            }
        }

    }


    PeerController(String pId,Socket s,int sessionType)
    {
        this.socket=s;
        this.sessionType=sessionType;
        this.currentpId=pId;
        try
        {
            ip=s.getInputStream();
            op=s.getOutputStream();
        }
        catch (IOException e) {
            ProcessPeer.logs.showLog(this.currentpId+" error while getting data");
        }
    }
    PeerController(String host,int port,int sessionType,String pId) throws IOException
    {
        this.sessionType=sessionType;
        try
        {
            this.currentpId=pId;
            this.socket=new Socket(host,port);
        }
        catch (Exception ex)
        {
            ProcessPeer.logs.showLog("Error Connecting "+pId);
        }
        try
        {
            ip=socket.getInputStream();
            op=socket.getOutputStream();
        }
        catch (IOException e) {
            ProcessPeer.logs.showLog(this.currentpId+" error getting data");
        }
    }
    public  boolean connectWithPeer()
    {
        try
        {
            op.write(Connection.HandShake_to_ByteArray(new Connection(Integer.parseInt(this.currentpId))));
        }
        catch (Exception ex)
        {
            ProcessPeer.logs.showLog("HandShake Error");
            return true;
        }
        return false;
    }



}

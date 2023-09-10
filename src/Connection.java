import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;

public class Connection
{
    //Connecrtion is a class with Handshake prompts of get and set Handshake Message , Handshake headers, Bytes and Bits data
    public int PeerId() {
        return peerId;
    }

    public void PeerID_set(int peerId) {
        this.peerId = peerId;
    }

    public String HandShakeHdr() {
        return handshakeHdr;
    }

    public void HandShake_set_header(String handShakeHeader) {
        this.handshakeHdr = handShakeHeader;
    }

    public String getZeroBits() {
        return zeroBits;
    }

    public void setZeroBits(String zeroBits) {
        this.zeroBits = zeroBits;
    }

    public byte[] HandShakeMsg() {
        return handshakeMsg;
    }

    public void sendHandShakeMsg(byte[] handShakeMessage) {
        this.handshakeMsg = handShakeMessage;
    }

    

    @Override
    public String toString() {
        return "Handshake{" +
                "handShakeMessage=" + Arrays.toString(handshakeMsg) +
                '}';
    }

    private String handshakeHdr;
    private String zeroBits;
    private int k;
    private byte[] handshakeMsg;
    private int peerId;
    

    public byte[] HandShakeByteheader() {
        return handShakeHeaderBytes;
    }

    public void HandShakeHeaderByte_Set(byte[] handShakeHeaderBytes) {
        this.handShakeHeaderBytes = handShakeHeaderBytes;
    }

    private byte[] handShakeHeaderBytes=new byte[32];
    Connection()
    {

    }
    // Establish Connection function with the Passed PeerId
    Connection(int peerId)
    {
        this.handshakeMsg=new byte[32];
        this.peerId=peerId;
        this.handshakeHdr=Payloadpiece.handshakeHeader;
        this.zeroBits=Payloadpiece.zeroBits;
        this.k=0;
        this.handShakeHeaderBytes=handshakeHdr.getBytes(StandardCharsets.UTF_8);
    }

    //Handshake generation and make connection between all the other peers to pass the messaging protocols with get handshake
    public void ConnectionHandshake()
    {
        String peerIdString=this.peerId+"";
        byte[] peerIdByteArray=peerIdString.getBytes(StandardCharsets.UTF_8);
        int k=0;
        byte[] HandshakeHdrArray=this.handshakeHdr.getBytes();
        byte[] ZeroBitsArray=this.zeroBits.getBytes(StandardCharsets.UTF_8);
        
        try
        {
            HandShakeMessageHeaderwithByteArray(HandshakeHdrArray);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
        }
        try
        {
            HandShakeMessagePaddng(ZeroBitsArray);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
        }
        try
        {
            handShakeMessagePeerID(peerIdByteArray);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
        }
        System.out.println("Message: "+ new String(this.handshakeMsg, StandardCharsets.UTF_8));
    }

    //Handshake generation and make connection between all the other peers to pass the messaging protocols with set handshake
    public void HandShakeMessageHeaderwithByteArray(byte[] handShakeHeaderByteArray)
    {
        try
        {
            if(handShakeHeaderByteArray==null )
            {
                throw new Exception("Invalid Handshake Header");
            }
            if(handShakeHeaderByteArray.length>18 )
            {
                throw new Exception(" HandShake Header too out of bounds");
            }

            for (int i = 0; i < handShakeHeaderByteArray.length; i++)
            {
                this.handshakeMsg[k] = handShakeHeaderByteArray[i];
                k++;
            }

        }

        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    public void HandShakeMessagePaddng(byte[] zeroBitsByteArray)
    {
        try
        {
            if (zeroBitsByteArray == null)
            {
                throw new Exception("Invalid Zero bit");
            }
            if(zeroBitsByteArray.length>10)
            {
               throw new Exception("Zero bit Value high");
            }
            for (int i = 0; i < zeroBitsByteArray.length; i++)
            {
                this.handshakeMsg[k] = zeroBitsByteArray[i];
                k++;
            }
        }
         catch (Exception e)
        {
        e.printStackTrace();
        System.out.println(e.getMessage());
        }

    }

    //Sets handhake message peerID
    public void handShakeMessagePeerID(byte[] peerIdByteArray)
    {
        try
        {
            if (peerIdByteArray == null)
            {
                throw new Exception("Invalid PeerID");
            }
            if (peerIdByteArray.length > 4)
            {
                throw new Exception("Zero Bit Value high");
            }

            for (int i = 0; i < peerIdByteArray.length; i++) {
                this.handshakeMsg[k] = peerIdByteArray[i];
                k++;
            }
        }
         catch (Exception e)
        {
        e.printStackTrace();
        System.out.println(e.getMessage());
        }

    }


    //Byte array to connection handshake that checks whether handshake has the valid length and if null also whether valid data present
    public static Connection ByteArray_HandShake(byte[] b)
    {

        byte[] mheader;
        byte[] mpeerId;
        Connection h;
        if(b.length!=PayLoad.sizeoOfHandShakeMessage)
        {
            ProcessPeer.logs.showLog("Handshake length Invalid");
            System.exit(0);
        }
        h=new Connection();
        mheader=new byte[PayLoad.sizeOfHeader];
        mpeerId=new byte[PayLoad.sizeOfPeerId];
        System.arraycopy(b,0,mheader,0,PayLoad.sizeOfHeader);
        System.arraycopy(b,PayLoad.sizeOfHeader+PayLoad.sizeofZerobits,mpeerId,0,PayLoad.sizeOfPeerId);
        h.HandShakeMessageHeaderwithByteArray(mheader);
        h.handShakeMessagePeerID(mpeerId);
        return h;
    }

    //              checks the HandShake header and makes check that if the Peer Bits and Zero bits
    //   from MessageData and PayloadPiece has valid values and thrn copy the handshake message data to bytearray
    public  static byte[] HandShake_to_ByteArray(Connection handshake)
    {
        byte[] m=new byte[PayLoad.sizeoOfHandShakeMessage];


        if(handshake.HandShakeHdr()==null || handshake.HandShakeHdr().length()>PayLoad.sizeOfHeader||handshake.HandShakeHdr().length()==0)
        {
            ProcessPeer.logs.showLog("Invalid Handshake Header");
            System.exit(0);
        }
        else
        {
            System.arraycopy(handshake.HandShakeHdr().getBytes(StandardCharsets.UTF_8),0,m,0,handshake.HandShakeHdr().length());
        }
        if(handshake.getZeroBits().isEmpty()||handshake.getZeroBits().length()>PayLoad.sizeofZerobits||handshake.getZeroBits()==null)
        {
            ProcessPeer.logs.showLog("Zero Bits Invalid");
            System.exit(0);
        }
        else
        {
            System.arraycopy(handshake.getZeroBits().getBytes(StandardCharsets.UTF_8),0,m,PayLoad.sizeOfHeader,PayLoad.sizeofZerobits);


        }
        if( (String.valueOf(handshake.PeerId())).length()>PayLoad.sizeOfPeerId)
        {
            ProcessPeer.logs.showLog("Peer Bits Invalid");
            System.exit(0);
        }
        else
        {
           System.arraycopy(String.valueOf(handshake.PeerId()).getBytes(StandardCharsets.UTF_8),0,m,PayLoad.sizeOfHeader+PayLoad.sizeofZerobits,PayLoad.sizeOfPeerId);
        }
        return m;
    }

}

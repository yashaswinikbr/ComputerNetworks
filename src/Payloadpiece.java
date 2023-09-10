
class Payloadpiece
{
    public int hasPiece;
    public String senderpId;
    public byte[] piece;
    public int pindx;
    public static final String zeroBits = "0000000000";
    public static final String handshakeHeader = "P2PFILESHARINGPROJ";
    public  static int numberOfPreferredNeighbors = 0;
    public  static int unchokingInterval=0;
    public  static int optimisticUnchokingInterval=0;
    public  static int fileSize=0;
    public  static int pieceSize=0;
    public  static String fileName="";

    public int  getHasPiece()
    {
        return hasPiece;
    }

    public void setHasPiece(int piece) {
        this.hasPiece = piece;
    }
    public String getSenderpId() {
        return senderpId;
    }

    public void setSenderpId(String senderpId) {
        this.senderpId = senderpId;
    }
    public Payloadpiece()
    {
        piece=new byte[Payloadpiece.pieceSize];
        pindx=-1;
        hasPiece=0;
        senderpId=null;
    }


}
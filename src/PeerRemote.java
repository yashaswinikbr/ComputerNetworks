import java.util.Comparator;
import java.util.Date;

public class PeerRemote implements Comparator<PeerRemote>
{
    public final boolean comparator=false;
    public String peerId;
    public String peerAddress;
    public String peerPort;
    public int isFirstPeer;
    public double streamRate = 0;
    public int isInterested = 1;
    public int isPreferredNeighbor = 0;
    public int isOptUnchokedNeighbor = 0;
    public int isChoked = 1;
    public PayLoad payloadData;
    public int state = -1;
    public int peerIndex;
    public int isCompleted = 0;
    public int isHandShake = 0;
    public Date sTime;
    public Date fTime;
     
    //remote peer to get set peer id, address, peer ports, whether file present or not , postion of peer in the connection
    public PeerRemote() {

    }

    public String getPeerId() {
        return peerId;
    }

    public void setPeerId(String peerId) {
        this.peerId = peerId;
    }

    public String getPeerAddress() {
        return peerAddress;
    }

    public void setPeerAddress(String peerAddress) {
        this.peerAddress = peerAddress;
    }

    public String getPeerPort() {
        return peerPort;
    }

    public void setPeerPort(String peerPort) {
        this.peerPort = peerPort;
    }

    public boolean isHasFile() {
        return hasFile;
    }

    public void setHasFile(boolean hasFile) {
        this.hasFile = hasFile;
    }

    public int getPeerPos() {
        return peerPos;
    }

    public void setPeerPos(int peerPos) {
        this.peerPos = peerPos;
    }


    public boolean hasFile;
    public int peerPos;
    public boolean isFirst;
    // Give peer remote with the get set functiona and add the respective data to the peer 
    public PeerRemote(String pId, String pAddress, String pPort,boolean hFile) {
        peerId = pId;
        peerAddress = pAddress;
        peerPort = pPort;
        hasFile=hFile;
        payloadData =new PayLoad();
    }
    public int compareTo(PeerRemote remotePeerInfo)
    {
        return Double.compare(this.streamRate,remotePeerInfo.streamRate);
    }
    
    //Basic compare function between 2 remote peers to check them whether they are null or present
    public int compare(PeerRemote p1,PeerRemote p2)
    {
        if(p1==null && p2==null)
            return 0;
        if(p1==null)
        {
            return 1;
        }
        if(p2==null)
        {
            return -1;
        }
        if(comparator)
        {
            return p1.compareTo(p2);
        }
        else
        {
            return p2.compareTo(p1);
        }
    }

}
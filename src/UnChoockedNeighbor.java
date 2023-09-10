import java.util.Collections;
import java.util.Enumeration;
import java.util.TimerTask;
import java.util.Vector;

public class UnChoockedNeighbor extends TimerTask {

    //Class Unchocked neighbor checks for the UNCHOCKED message from the neighoring peers,
    //               and hepls in passing the data pieces from peer to peer
    public void run() {
        ProcessPeer.readNextPeerData();
        if(!ProcessPeer.NeighborHashMap_Unchocked.isEmpty())
            ProcessPeer.NeighborHashMap_Unchocked.clear();
        Enumeration<String> remotePeerIds = Collections.enumeration(ProcessPeer.PeerHashMap.keySet());
        Vector<PeerRemote> remotePeerVector = new Vector<>();

        //Checks whether remote peer has more elemets to transfer and maintain the connection and status with other peers

        while(remotePeerIds.hasMoreElements()) {
            String key = remotePeerIds.nextElement();
            PeerRemote remotePeerInfo = ProcessPeer.PeerHashMap.get(key);
            if (remotePeerInfo.isChoked == 1
                    && !key.equals(ProcessPeer.peerId)
                    && remotePeerInfo.isCompleted == 0
                    && remotePeerInfo.isHandShake == 1)
                remotePeerVector.add(remotePeerInfo);
        }

        //Remotepeervector contains the vectors with the peers that need to be unchocked and the data to be 
        //  passed through the neighbouring peers and establish a valid data transfer between the peers 
        if (remotePeerVector.size() > 0) {
            Collections.shuffle(remotePeerVector);
            PeerRemote intialPeer = remotePeerVector.firstElement();
            ProcessPeer.PeerHashMap.get(intialPeer.peerId).isOptUnchokedNeighbor = 1;
            ProcessPeer.NeighborHashMap_Unchocked.put(intialPeer.peerId, ProcessPeer.PeerHashMap.get(intialPeer.peerId));
            ProcessPeer.logs.showLog( ProcessPeer.peerId+" has Unchocked Neighbor "+intialPeer.peerId);

            //Check the peerHashmap of main peer connections to check the condition of chocked and unchocked
            if (ProcessPeer.PeerHashMap.get(intialPeer.peerId).isChoked == 1) {
                ProcessPeer.PeerHashMap.get(intialPeer.peerId).isChoked = 0;
                ProcessPeer.sendRequestToUnchoke( intialPeer.peerId,ProcessPeer.peerData.get(intialPeer.peerId));
                ProcessPeer.sendHaveMessage(intialPeer.peerId,ProcessPeer.peerData.get(intialPeer.peerId));
                ProcessPeer.PeerHashMap.get(intialPeer.peerId).state = 3;
            }
        }
    }
}
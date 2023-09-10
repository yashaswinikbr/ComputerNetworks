import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.TimerTask;

public class PreferNeighbours extends TimerTask {

    //Preferred nighbor class taht allows the neighbors to make the hashmap addition to maintain the valid preferred neighbor hashmap,
    //           which inturn we use at the data passing from one peer to another
    public void run() {
        int pinterested = 0;
        StringBuilder s = new StringBuilder();
        ProcessPeer.readNextPeerData();
        Enumeration<String> peerIds = Collections.enumeration(ProcessPeer.PeerHashMap.keySet());
        
        //condition to check that peerids and if already presnt peer ids in neighbor set,
        //   then we dont add them or we maintain them as neighbor data in the list
        while(peerIds.hasMoreElements()) {
            String remotePeerId = peerIds.nextElement();
            PeerRemote remotepeer = ProcessPeer.PeerHashMap.get(remotePeerId);
            if(remotePeerId.equals(ProcessPeer.peerId)) continue;
            if (remotepeer.isCompleted == 0 && remotepeer.isHandShake == 1)
                pinterested++;
            else if(remotepeer.isCompleted == 1) {
                try {
                    ProcessPeer.NeighbourHashMap.remove(remotePeerId);
                }
                catch (Exception ignored) { }
            }
        }

        //checks with payload piece data so thet we can use this data for peer in NeighborHashmap
        if(pinterested > PayLoad.piece) 
        {
            
            if(!ProcessPeer.NeighbourHashMap.isEmpty())
                ProcessPeer.NeighbourHashMap.clear();

            List<PeerRemote> remotePeersarrayList = new ArrayList<>(ProcessPeer.PeerHashMap.values());
            remotePeersarrayList.sort(new PeerRemote());
            int Count = 0;

            for (PeerRemote remotePeerInfo : remotePeersarrayList) 
            {
                if (Count > Payloadpiece.numberOfPreferredNeighbors - 1) break;

                if (remotePeerInfo.isHandShake == 1 && !remotePeerInfo.peerId.equals(ProcessPeer.peerId)
                        && ProcessPeer.PeerHashMap.get(remotePeerInfo.peerId).isCompleted == 0) 
                        {
                    ProcessPeer.PeerHashMap.get(remotePeerInfo.peerId).isPreferredNeighbor = 1;
                    ProcessPeer.NeighbourHashMap.put(remotePeerInfo.peerId, ProcessPeer.PeerHashMap.get(remotePeerInfo.peerId));
                    Count++;
                    s.append(remotePeerInfo.peerId).append(", ");

                    if (ProcessPeer.PeerHashMap.get(remotePeerInfo.peerId).isChoked == 1) 
                    {
                        ProcessPeer.sendRequestToUnchoke(remotePeerInfo.peerId,ProcessPeer.peerData.get(remotePeerInfo.peerId) );
                        ProcessPeer.PeerHashMap.get(remotePeerInfo.peerId).isChoked = 0;
                        ProcessPeer.sendHaveMessage(remotePeerInfo.peerId,ProcessPeer.peerData.get(remotePeerInfo.peerId));
                        ProcessPeer.PeerHashMap.get(remotePeerInfo.peerId).state = 3;
                    }
                }
            }
        }
        else
        {
            //Message passing condtition for all the choke, unchoke,have ,send , recieve and bitfield
            peerIds = Collections.enumeration(ProcessPeer.PeerHashMap.keySet());
            while(peerIds.hasMoreElements())
            {
                String nextPeerId = peerIds.nextElement();
                PeerRemote remotePeer = ProcessPeer.PeerHashMap.get(nextPeerId);
                if(nextPeerId.equals(ProcessPeer.peerId)) continue;

                if (remotePeer.isCompleted == 0 && remotePeer.isHandShake == 1) {
                    if(!ProcessPeer.NeighbourHashMap.containsKey(nextPeerId)) {
                        s.append(nextPeerId).append(", ");
                        ProcessPeer.NeighbourHashMap.put(nextPeerId, ProcessPeer.PeerHashMap.get(nextPeerId));
                        ProcessPeer.PeerHashMap.get(nextPeerId).isPreferredNeighbor = 1;
                    }
                    if (remotePeer.isChoked == 1) {
                        ProcessPeer.sendRequestToUnchoke(nextPeerId,ProcessPeer.peerData.get(nextPeerId));
                        ProcessPeer.PeerHashMap.get(nextPeerId).isChoked = 0;
                        ProcessPeer.sendHaveMessage(nextPeerId,ProcessPeer.peerData.get(nextPeerId));
                        ProcessPeer.PeerHashMap.get(nextPeerId).state = 3;
                    }
                }
            }
        }
        if (!s.toString().equals(""))
            ProcessPeer.logs.showLog( ProcessPeer.peerId+" selected preferred neighbor "+ s);
    }
}
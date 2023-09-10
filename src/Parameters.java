public class Parameters {

    //Basic parameter class which is also kind majorly wokrs on message data thet helps in get, set message data ,  PeerID
    public MessageData getM() {
        return m;
    }

    public void setM(MessageData m) {
        this.m = m;
    }

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    MessageData m;
    String pId;

    Parameters() {
        m = new MessageData();
        pId = null;
    }

}
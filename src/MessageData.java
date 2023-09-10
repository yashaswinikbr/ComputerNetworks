import java.nio.charset.StandardCharsets;

public class MessageData {


    private byte[] dataArray;
    private byte[] dataLengthArray;
    private byte[] payLoadArray;
    private String dataLength;
    private String dataType;
    private int lengthOfMessage = PayLoad.typeOfMessage;

    //Message Data class that work on Get and set of Lengthof Message , Dataarraylength, dataArray and data type
    MessageData() {

    }
    public int getLengthOfMessage() {
        return lengthOfMessage;
    }

    public void setLengthOfMessage(int lengthOfMessage) {
        this.lengthOfMessage = lengthOfMessage;
    }

    public byte[] getDataLengthArray() {
        return dataLengthArray;
    }

    public void setDataLengthArray(byte[] dataLengthArray) {
        this.dataLengthArray = dataLengthArray;
    }

    public byte[] getDataArray() {
        return dataArray;
    }

    public void setDataArray(byte[] dataArray) {
        this.dataArray = dataArray;
    }

    public String getDataType() {
        return dataType;
    }

    //checks the message condtion to then work on that respecive condtion 
    // function to make the flow of data simple and feasible in between the peers
    MessageData(int n) {
        try {
            if ((n == PayLoad.choke) || (n == PayLoad.unChoke) || (n == PayLoad.intersted) || (n == PayLoad.notInterested)) {
                this.setDataType("" + n);
                this.payLoadArray = null;
                this.lengthOfMessage = 1;
                this.dataLength = this.lengthOfMessage + "";
                this.dataLengthArray = PayLoad.Int_to_Byte(this.lengthOfMessage);
            } else {
                System.out.println("Message is Not Valid");
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    //checks the message condtion to then work on that respecive condtion along with data array
    // function to make the flow of data simple and feasible in between the peers 
    MessageData(int n, byte[] arr) {
        try {
            if (arr == null) {
                if ((n == PayLoad.choke) || (n == PayLoad.unChoke) || (n == PayLoad.intersted) || (n == PayLoad.notInterested)) {
                    this.lengthOfMessage = 1;
                    this.dataLength = this.lengthOfMessage + "";
                    this.dataLengthArray = PayLoad.Int_to_Byte(this.lengthOfMessage);
                    this.payLoadArray = null;
                } else {
                    System.out.println("No payload");
                }

            } else {
                this.lengthOfMessage = arr.length + 1;
                this.dataLength = this.lengthOfMessage + "";
                this.dataLengthArray = PayLoad.Int_to_Byte(this.lengthOfMessage);
                if (this.dataLengthArray.length > PayLoad.sizeOfMessage) {
                    System.out.println("Messge is large");
                }
                this.payLoadArray = arr;
            }
            this.setDataType(""+n);

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

   
    // Message data functions get and set of Data length, Payload array
    public void setDataType(byte[] data) {
        this.dataType = new String(data, StandardCharsets.UTF_8);
        this.dataArray = data;
    }

    public void setDataType(String data) {
        this.dataType = data.trim();
        this.dataArray = this.dataType.getBytes(StandardCharsets.UTF_8);
    }

    public String getDataLength() {
        return dataLength;
    }

    public void setDataLength(byte[] b) {
        int l = PayLoad.ByteArray_to_Int(b, 0);
        this.dataLength = "" + l;
        this.dataLengthArray = b;
        this.lengthOfMessage = l;
    }

    public void setDataLength(String data) {
        this.lengthOfMessage = Integer.parseInt(data);
        this.dataLength = data;
        this.dataLengthArray = PayLoad.Int_to_Byte(this.lengthOfMessage);
    }

    public byte[] getPayLoadArray() {
        return payLoadArray;
    }

    public void setPayLoadArray(byte[] payLoadArray) {
        this.payLoadArray = payLoadArray;
    }

    //Data to byte array conversion function which converts the given data file to byte array
    public static byte[] DataToByte(MessageData m) {
        byte[] dataByteArray;
        int dType;
        try {
            dType = Integer.parseInt(m.getDataType());
            if ((m.getDataArray() == null) || ((dType < 0) || dType > 7) || (m.getDataLengthArray().length > PayLoad.sizeOfMessage) || (m.getDataLengthArray() == null)) {
                throw new Exception("Message is Not Valid");
            }
            if (m.getPayLoadArray() == null) {
                dataByteArray = new byte[PayLoad.sizeOfMessage + PayLoad.typeOfMessage];
                System.arraycopy(m.getDataLengthArray(), 0, dataByteArray, 0, m.getDataLengthArray().length);
                System.arraycopy(m.getDataArray(), 0, dataByteArray, PayLoad.sizeOfMessage, PayLoad.typeOfMessage);
            } else {
                dataByteArray = new byte[PayLoad.sizeOfMessage + PayLoad.typeOfMessage + m.getPayLoadArray().length];
                System.arraycopy(m.getDataLengthArray(), 0, dataByteArray, 0, m.getDataLengthArray().length);
                System.arraycopy(m.getDataArray(), 0, dataByteArray, PayLoad.sizeOfMessage, PayLoad.typeOfMessage);
                System.arraycopy(m.getPayLoadArray(), 0, dataByteArray, PayLoad.sizeOfMessage + PayLoad.typeOfMessage, m.getPayLoadArray().length);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            dataByteArray = null;
        }
        return dataByteArray;
    }
}


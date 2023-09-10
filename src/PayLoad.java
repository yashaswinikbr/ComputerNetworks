import java.io.*;

public class PayLoad
{
    public Payloadpiece[] pieceData;
    public int bSize;
    public static LogData l;
    public static final int maxPieceLength=4;
    public static final int sizeoOfHandShakeMessage=32;
    public static final int sizeOfHeader=18;
    public static final int sizeofZerobits=10;
    public static final int sizeOfPeerId=4;
    public static final int sizeOfMessage=4;
    public static final int typeOfMessage=1;
    public static final int choke=0;
    public static final int unChoke=1;
    public static final int intersted=2;
    public static final int notInterested=3;
    public static final int have=4;
    public static final int bitField=5;
    public static final int request=6;
    public static final int piece=7;

    // Payload class maintains the data and also the values to be passed and fetched with respect to the files
    PayLoad()
    {
        double d=(double)Payloadpiece.fileSize/Payloadpiece.pieceSize;
        this.bSize=(int)Math.ceil(d);
        this.pieceData=new Payloadpiece[this.bSize];
        int i=0;
        while(i<this.bSize)
        {
            this.pieceData[i++]=new Payloadpiece();
        }
    }
    public int getbSize() {
        return bSize;
    }

    public void setbSize(int bSize) {
        this.bSize = bSize;
    }
    public Payloadpiece[] getPieceData() {
        return pieceData;
    }

    public void setPieceData(Payloadpiece[] pieceData) {
        this.pieceData = pieceData;
    }
    public synchronized boolean comparePayLoadData(PayLoad p)
    {
        int csize=p.getbSize();
        int i=0;
        while(i<csize)
        {
            if(p.getPieceData()[i].getHasPiece()==1 && this.getPieceData()[i].getHasPiece()==0)
            {
                return true;
            }
            i++;
        }
        return false;
    }

    //Fetches bitfield data for the respective payload
    public synchronized int fetchFirstBitField(PayLoad p)
    {
        if(this.getbSize()>=p.getbSize())
        {
            int i=0;
            while(i<p.getbSize())
            {
                if(p.getPieceData()[i].getHasPiece()==1 && this.getPieceData()[i].getHasPiece()==0)
                {
                    return i;
                }
                i++;
            }
        }
        else {
            int i = 0;
            while (i < this.getbSize())
            {
                if (p.getPieceData()[i].getHasPiece() == 1 && this.getPieceData()[i].getHasPiece() == 0)
                {
                    return i;
                }
                i++;
            }
        }
        return -1;
    }

    //Encodes the obtained data from the peer and give the encode bytes/bits of data
    public byte[] encodeData()
    {
        int s=0;
        if(this.bSize%8!=0)
        {
            s+=1;
        }
         s+=this.bSize/8;
        byte[] bArray=new byte[s];
        int temp=0;
        int bindx=0;
        int i=1;
        while(i<=this.bSize)
        {
            int t1=this.pieceData[i-1].hasPiece;
            temp=temp<<1;
            if(t1==1)
            {
                temp++;
            }
            if(i%8==0)
            {
                bArray[bindx]=(byte) temp;
                bindx++;
                temp=0;
            }
            i++;
        }
        i--;
        if(i%8!=0)
        {
            int shift=this.bSize-(this.bSize/8)*8;
            temp<<=(8-shift);
            bArray[bindx]=(byte)temp;
        }
        return bArray;
    }
    public static PayLoad decodeData(byte[] b)
    {
       PayLoad p=new PayLoad();
       int i=0;
       while(i<b.length)
       {
           int c=7;
           while(c>=0)
           {
             int pNo=1<<c;
             int k=i*8+(8-c-1);
             if(k<p.bSize)
             {

                 if((b[i]&(pNo))!=0)
                 {
                    p.pieceData[k].hasPiece=1;
                 }
                 else
                 {
                     p.pieceData[k].hasPiece=0;
                 }
             }
             c--;
           }
           i++;
       }
       return p;
    }
    public boolean hasAllPieces()
    {

        for(int i=0;i<this.bSize;i++)
        {
            if(this.pieceData[i].hasPiece==0)
            {
                return false;
            }
        }
        return true;

    }


    public void initPayLoad(String pId, boolean hasFile)
    {
        int i=0;
        if(hasFile)
        {
            while(i<bSize)
            {
                this.pieceData[i].setHasPiece(1);
                this.pieceData[i].setSenderpId(pId);
                i++;
            }

        }
        else
        {
            while(i<bSize)
            {
                this.pieceData[i].setHasPiece(0);
                this.pieceData[i].setSenderpId(pId);
                i++;
            }
        }

    }
    public int avaliablePieces()
    {
        int pc=0;
        for(int i=0;i<this.bSize;i++)
        {
            if(this.pieceData[i].hasPiece==1)
            {
                pc+=1;
            }
        }
        return pc;
    }

    //checks whether the paylpoad contains that data piece and gives the information of 
    //      the downloaded pieces of data and then manages whether the downloaded files present or not
    public synchronized  void updatePayLoad(Payloadpiece p, String pId )  {
        if(ProcessPeer.PayloadCurrent.pieceData[p.pindx].hasPiece==1)
        {
            ProcessPeer.logs.showLog(pId+" piece is present");
        }
        else
        {
            try {
                byte[] writeData;
                int offset = p.pindx * Payloadpiece.pieceSize;
                File f = new File(ProcessPeer.peerId, Payloadpiece.fileName);
                RandomAccessFile r = new RandomAccessFile(f, "rw");
                writeData = p.piece;
                r.seek(offset);
                r.write(writeData);
                r.close();
                this.pieceData[p.pindx].setHasPiece(1);
                this.pieceData[p.pindx].setSenderpId(pId);
                ProcessPeer.logs.showLog(
                        ProcessPeer.peerId + " downloaded piece " + p.pindx + " from " + pId + " contains " + ProcessPeer.PayloadCurrent.avaliablePieces() + " pieces");
                if (ProcessPeer.PayloadCurrent.hasAllPieces()) {
                    ProcessPeer.PeerHashMap.get(ProcessPeer.peerId).isInterested = 0;
                    ProcessPeer.PeerHashMap.get(ProcessPeer.peerId).isCompleted = 1;
                    ProcessPeer.PeerHashMap.get(ProcessPeer.peerId).isChoked = 0;
                    updatePeerConfig(ProcessPeer.peerId);
                    ProcessPeer.logs.showLog(ProcessPeer.peerId + " downloaded file");
                    ProcessPeer.logs.showLog(ProcessPeer.peerId + " sent Not Inserted");
                }
            }
            catch (Exception ex)
            {
                ProcessPeer.logs.showLog(ex.getMessage());
            }
        }
    }

    //Peer config values update for the files available or not after the every peer recieves the data from the other peer
    public void updatePeerConfig(String pId)
    {
        String str="";
        String l;
        BufferedReader br;
        BufferedWriter bw;
        try
        {
             br=new BufferedReader(new FileReader(ProcessPeer.Peer_path));
            while((l=br.readLine())!=null)
            {
               String[] st=l.trim().split(" ");
                if(st[0].equals(pId))
                {
                    st[3]="1";
                    l=st[0]+" "+st[1]+" "+st[2]+" "+st[3];
                }
                str+=l+"\n";
            }
            br.close();
            bw=new BufferedWriter(new FileWriter(ProcessPeer.Peer_path));
            bw.write(str);
            bw.close();

        }
        catch (Exception ex)
        {
            System.out.println(ex.getMessage());
        }
    }
    public static byte[] Int_to_Byte(int val)
    {
       byte[] b=new byte[4];
       int i=0;
       while(i<4)
       {
           int o=(b.length-1-i)*8;
           b[i]=(byte)((val>>>o)&(0xFF));
           i++;
       }
       return b;
    }
    public static int ByteArray_to_Int(byte[] data, int offset)
    {
        int res=0;
        for(int j=0;j<4;j++)
        {
            int s=(3-j)*8;
            res+=(data[j + offset] & 0x000000FF) << s;
        }
        return res;
    }

}

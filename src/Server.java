import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable
{
    //Server Class for the socket creation and check data from ports to establish connection
    private final ServerSocket socket;
    private final String peerId;
    Socket remoteSocket;
    Thread thread;

    //Server Construction function to create socket and based for different peers
    public Server(ServerSocket s,String peerId)
    {
        this.socket=s;
        this.peerId=peerId;
    }

    //server run function to start the server thread up and running
    public void run()
    {
        while(true)
        {
            try
            {
                remoteSocket=socket.accept();
                
                //creating new thread for the respective peerid passed
                thread=new Thread(new PeerController(this.peerId,remoteSocket,0));
                ProcessPeer.socket_thread.add(thread);
                thread.start();
            }
            catch (Exception ex)
            {
                //catch exception if the server socket creation error was made
                ProcessPeer.logs.showLog(this.peerId+" connection error");
            }
        }
    }
}

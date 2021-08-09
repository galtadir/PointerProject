import java.io.IOException;
import java.net.Socket;


//represent client in the game in the client side
public class Player {

    public static void main(String[] args) {
        Socket socket = null;

        try {
            //init the socket, the read thread and the write thread for getting input from the user and server simultaneously
            socket = new Socket("localhost",5000);
            Read r = new Read(socket);
            Write w = new Write(socket);
            r.join();
            w.join();
        } catch (IOException | InterruptedException e) {
            System.out.println("Exception:" + e.getMessage());
        } finally{
            try {
                if(socket!=null)
                socket.close();
            }
            catch (Exception e) {
                System.out.println("Exception:" + e.getMessage());
            }
        }

    }

}

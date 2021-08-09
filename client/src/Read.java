import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

//responsible for getting the input from the server and print for the user
public class Read extends Thread{

    private BufferedReader in;

    Read(Socket s) throws IOException {
        this.in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        start();
    }

    public void run(){
        try {
            String line;
            while (true){
                line = in.readLine();
                if (line.equals("exit"))
                    break;
                System.out.println(line);
                if(line.equals("The game is over")){
                    System.exit(0);
                }
            }
        } catch (IOException e) {
            System.out.println("Read class READ error:" +e.getMessage());
        }
    }
}

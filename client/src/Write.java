import java.io.*;
import java.net.Socket;

//responsible for getting input from the user and send to the server
class Write extends Thread{
    private PrintWriter out;
    private BufferedReader in;
    Write(Socket s) throws IOException {
        this.out = new PrintWriter(new OutputStreamWriter(s.getOutputStream()),true);
        this.in = new BufferedReader(new InputStreamReader(System.in));
        start();
    }
    public void run(){
        try {
            String line;
            while(true){
                line = in.readLine();
                out.println(line);
                if (line.equals("exit")){
                    System.exit(0);
                }

            }
        } catch (IOException e) {
            System.out.println("Write class Read Error:" + e.getMessage());
        }


    }
}

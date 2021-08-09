import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

public class Server {

    public static void main(String[] args){

        Dealer dealer = new Dealer(args);
        dealer.start();
    }
}

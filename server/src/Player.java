import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

//this class represent a player in the game, each player get a thread that responsible for transfer the data
public class Player extends Thread {
    private Socket socket;
    private String name;
    private BufferedReader input;
    private PrintWriter output;
    private Dealer dealer;
    //represent the amount fo money that left for the user
    private int wallet;


    Player(Socket socket, Dealer dealer, String name){
        this.socket = socket;
        this.dealer = dealer;
        this.name= name;
        this.wallet = 0;
        try{
            this.input = new BufferedReader((new InputStreamReader(socket.getInputStream())));
            this.output = new PrintWriter(socket.getOutputStream(),true);
        }catch (IOException e){
            System.out.println("exception... " + e.getMessage());
        }
    }



    @Override
    public void run() {
        try{
            String nextInput;
            while (true){
                //waiting for input from the client
                nextInput = input.readLine();
                if(nextInput.equals("exit")){
                    dealer.removePlayer(this);
                    break;
                }
                try {
                    //try to parse the string for int
                    int bid = Integer.parseInt(nextInput);
                    //check if there any item for sell
                    if(dealer.getCurrentBid()==null){
                        send("a Bidding-War has not started yet");
                    }
                    //check if the bidding is highest enough
                    else if(bid >= dealer.getCurrentBid().getStartingPrice() && bid>dealer.getCurrentBid().getSellingPrice() ){
                        //check if the bidder has enough money
                        if(bid<=wallet){
                            dealer.makeABid(bid,this);
                        }
                        else {
                            send("you do not have enough money");
                        }

                    }
                    else {
                        send("Your bid is not the highest");
                    }
                } catch (NumberFormatException ex) {
                    send("invalid input");
                }



            }

        }catch (IOException e){
            System.out.println("exception... " + e.getMessage());
        }
        finally {
            try{
                socket.close();
            }catch (IOException e){
                System.out.println("exception... " + e.getMessage());
            }

        }
    }

    int getWallet() {
        return wallet;
    }

    void setWallet(int wallet) {
        this.wallet = wallet;
        send("you have $" + wallet + " in you'r wallet");
    }

    //send the message to the client
    void send(String msg){
        output.println(msg);
    }


    String getNameString() {
        return name;
    }
}

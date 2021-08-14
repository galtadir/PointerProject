import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

//this class run on another thread and waiting get new users for the game
public class Login extends Thread{
    //the players that connect and waiting for enter the game
    private ArrayList<Player> waitingPlayers;
    //count the number of players current in the game for player name
    private int counter;
    private Dealer dealer;
    private long createdMillis;

    Login(Dealer dealer){
        waitingPlayers = new ArrayList<>();
        counter=1;
        this.dealer = dealer;
        this.createdMillis = System.currentTimeMillis();
    }


    @Override
    public void run() {

        try(ServerSocket serverSocket = new ServerSocket(5000)){
            while (true){
                String newPlayerName = "player " + (counter);
                counter++;
                Player player = new Player(serverSocket.accept(),dealer , newPlayerName);
                int age = getAgeInSeconds();
                //announce about the new player
                String playerMsg = newPlayerName+ " join in "+age + " sec";
                dealer.sendMsgToAllClient(playerMsg);
                sendMsgToAllWaiting(playerMsg);
                waitingPlayers.add(player);
                player.start();
                String msg = "You are Player" +player.getNameString();
                if(dealer.getStatus()==GameStatus.WaitingForBegin){
                    msg+= ", we are waiting for the game to begin.";
                }
                else if(dealer.getStatus()==GameStatus.InAuction){
                    msg+=", we are waiting for a Bidding-War on Item"+dealer.getItemNumber()+ " to end and then you will be joining the game.";
                }
                player.send(msg);
                player.send("You can enter Exit at any time to exit the game");

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMsgToAllWaiting(String msg){
        for(Player p: waitingPlayers){
            p.send(msg);
        }
    }

    private int getAgeInSeconds() {
        long nowMillis = System.currentTimeMillis();
        return (int)((nowMillis - this.createdMillis) / 1000);
    }


    ArrayList<Player> getWaitingPlayers() {
        return waitingPlayers;
    }
}

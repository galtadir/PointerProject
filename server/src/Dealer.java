import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

//the main class that manage the game
public class Dealer extends Thread {
    //save the active players in the game
    private ArrayList<Player> activePlayers;
    private Login login;
    //params for initialize the game
    private HashMap<Character,Integer> params;
    //save the starting price for each item
    private int[] prices;
    private Scanner sc = new Scanner(System.in);
    //save the current bid
    private Bid currentBid;
    //save the history of all bid in the current game
    private HashSet<Bid> historyBids;
    //save the money that have to add for each player that join the game for each phase
    private int[] moneyToWallet;
    //the current status of the game
    private GameStatus status;
    //the number of the item that current si bidding
    private int itemNumber;



    Dealer(String[] args){
        activePlayers = new ArrayList<>();
        login = new Login(this);
        login.start();
        historyBids = new HashSet<>();
        params = getParams(args);
        System.out.println("Welcome dealer");
        status = GameStatus.WaitingForBegin;
    }


    @Override
    public void run(){
        try{
            //each loop represent mini game
            while (true){
                //get items and price for each item
                prices = getPricesArray(params.get('n'), params.get('m'),params.get('x'), params.get('y'));
                //add money to players wallet
                for(Player player:activePlayers){
                    player.setWallet(player.getWallet() + moneyToWallet[0]);
                }
                //waiting for players to join the game
                waitingForPlayers();
                sendMsgToAllClient("The Mini-Game is starting..... " + activePlayers.size()+ " participant");
                status = GameStatus.InAuction;
                //start the mini game
                miniGame();
                status = GameStatus.WaitingForBegin;

                sendMsgToAllClient("The Mini-Game is over! ");
                sendMsgToAllClient("Waiting for dealer");
                System.out.println("Please enter exit to finish or anything else to continue ");
                //ask the dealer if he want to start another game
                String line  = sc.nextLine();
                if(line.equals("exit")){
                    printStat();
                    System.exit(0);
                }
                sendMsgToAllClient("New game is about to start in 10 seconds");
                Thread.sleep(10000);

            }



        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    //print some stat of the game
    private void printStat(){
        sendMsgToAllClient("The game is over");
        int itemsSold = 0;
        int itemsNotSold = 0;
        int totalItems = 0;
        int totalStartingPrice = 0;
        int totalSoldPrice = 0;
        HashMap<String,Integer> playersItemsCounter = new HashMap<>();
        for(Bid bid:historyBids){
            totalItems++;
            totalStartingPrice+=bid.getStartingPrice();
            if(bid.isSold()){
                itemsSold++;
                totalSoldPrice+=bid.getSellingPrice();
                String buyerName = bid.getBuyer().getNameString();
                if(playersItemsCounter.containsKey(buyerName)){
                    playersItemsCounter.put(buyerName,1+playersItemsCounter.get(buyerName));
                }
                else {
                    playersItemsCounter.put(buyerName,1);
                }
            }
            else {
                itemsNotSold++;
            }
        }
        System.out.println("Total "+ totalItems+ " items were auctions, total starting value: $" + totalStartingPrice);
        System.out.println(itemsSold + " items were sold for $"+ totalSoldPrice + ", " + itemsNotSold+" not sold");
        for(String player :playersItemsCounter.keySet()){
            System.out.println(player + " buy " +playersItemsCounter.get(player) + " items" );
        }
    }

    //adding the waiting players to the game
    private void transferPlayer(ArrayList<Player> waitingPlayers, int i){
        if(waitingPlayers.size()>0){
            for(Player player: waitingPlayers){
                player.setWallet(player.getWallet() + moneyToWallet[i]);
                activePlayers.add(player);
            }
            waitingPlayers.clear();
        }
    }

    //checking if there at least one player and the game, if not wait for them to connect
    private void waitingForPlayers() throws InterruptedException {
        while (activePlayers.size()<1){
            Thread.sleep(1);
            ArrayList<Player> waitingPlayers = login.getWaitingPlayers();
            if(waitingPlayers.size()>0){
                transferPlayer(waitingPlayers, 0);
                sendMsgToAllClient("The game will start in 10 seconds");
                //wait 10 second and check again if new players waiting
                Thread.sleep(10000);
                waitingPlayers = login.getWaitingPlayers();
                if(waitingPlayers.size()>0){
                    transferPlayer(waitingPlayers, 0);
                }
                break;
            }

        }
    }

    //send msg for all active players
    void sendMsgToAllClient(String msg){
        System.out.println(msg);
        for(Player p: activePlayers){
            p.send(msg);
        }
    }

    //send each item for bidding war
    private void miniGame() throws InterruptedException {
        for(int i=0 ; i<prices.length; i++){
            Thread.sleep(1);
            ArrayList<Player> waitingPlayers = login.getWaitingPlayers();
            if(waitingPlayers.size()>0){
                transferPlayer(waitingPlayers, i);
            }
            biddingWar(i+1, prices[i]);
        }

    }

    //start a bidding war for the given item and waiting 20 sec for bidding
    private void  biddingWar(int item , int price) throws InterruptedException {
        itemNumber = item;
        currentBid = new Bid(price);
        sendMsgToAllClient("The current item for sale is item " + item+ " for a starting price of " + price);
        sendMsgToAllClient("20 seconds left");
        Thread.sleep(10000);
        sendMsgToAllClient("10 seconds left");
        Thread.sleep(10000);
        historyBids.add(currentBid);
        if(currentBid.isSold()){
            Player buyer = currentBid.getBuyer();
            sendMsgToAllClient(currentBid.getBuyer().getNameString() + " won!");
            currentBid.getBuyer().setWallet(buyer.getWallet()-currentBid.getSellingPrice());
        }
        else {
            sendMsgToAllClient("item " + item + " not sold....");
        }

        currentBid = null;

    }

    Bid getCurrentBid(){
        return currentBid;
    }

    //make new bid for item
    void makeABid(int bid, Player player){
        if( bid>=currentBid.getStartingPrice() && bid>currentBid.getSellingPrice()){
            currentBid.setBuyer(player);
            currentBid.setSold(true);
            currentBid.setSellingPrice(bid);
            sendMsgToAllClient(player.getNameString() + " bid $" + bid + " for current item");
        }
    }

    //remove player from active players
    void removePlayer(Player player){
        player.send("goodbye");
        activePlayers.remove(player);
        sendMsgToAllClient(player.getNameString() + " left");
    }


    //responsible for extract the params for init the game
    private HashMap<Character,Integer> getParams(String[] args){
        HashMap<Character,Integer> params = new HashMap<>();
        params.put('n',10);
        params.put('m',20);
        params.put('x',100);
        params.put('y',2000);

        try {
            File myObj = new File("Dealer.config");

            Scanner myReader = new Scanner(myObj);
            while (true) {
                String data = myReader.nextLine();
                if(data.equals("}")){
                    break;
                }
                if(data.contains(":")){
                    data = data.replace(",","");
                    try{
                        int number = Integer.parseInt(data.substring(2));
                        if(number>0)
                            params.put(data.charAt(0),number);
                    }catch (Exception ignored){
                    }
                }
            }
            myReader.close();
        } catch (FileNotFoundException ignored) {
        }
        if(args.length>0){
            try{
                int number = Integer.parseInt(args[0]);
                if(number>0)
                    params.put('n',number);
            }catch (Exception ignored){
            }
        }
        if(args.length>1){
            try{
                int number = Integer.parseInt(args[1]);
                if(number>0)
                    params.put('m',number);
            }catch (Exception ignored){
            }
        }
        if(args.length>2){
            try{
                int number = Integer.parseInt(args[2]);
                if(number>0)
                    params.put('x',number);
            }catch (Exception ignored){
            }
        }
        if(args.length>3){
            try{
                int number = Integer.parseInt(args[3]);
                if(number>0)
                    params.put('y',number);
            }catch (Exception ignored){
            }
        }


        return params;
    }


    GameStatus getStatus() {
        return status;
    }

    int getItemNumber() {
        return itemNumber;
    }

    //init and return the prices for each item
    private int[] getPricesArray(int n, int m, int x, int y){
        int numOfItems = (int) ((Math.random() * (m - n)) + n);
        int[] prices = new int[numOfItems];
        System.out.println("Auction items: ");
        for(int i=0; i<prices.length; i++){
            int currentPrice = (((int) ((Math.random() * (y - x)) + x)+5)/10)*10;
            prices[i] = currentPrice;
            System.out.format("%6S %3S %5S", "Item" + (i+1) , " = "+ " $", currentPrice);
            System.out.println();
//            System.out.println("Item" + (i+1) + " = " + "$" + currentPrice);
        }
        moneyToWallet = new int[prices.length];
        moneyToWallet[moneyToWallet.length-1] = prices[moneyToWallet.length-1];
        for(int i=prices.length-2; i>=0 ; i--){
            moneyToWallet[i] = moneyToWallet[i+1] + prices[i];
        }
        return prices;
    }




}

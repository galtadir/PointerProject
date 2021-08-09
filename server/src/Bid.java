//this class represent a bid in the auction
public class Bid {
    private int startingPrice;
    private boolean sold;
    private Player buyer;
    private int sellingPrice;

    public Bid(int startingPrice) {
        this.startingPrice = startingPrice;
        this.sold = false;
        this.sellingPrice = 0;
    }

    public int getStartingPrice() {
        return startingPrice;
    }


    public boolean isSold() {
        return sold;
    }

    public void setSold(boolean sold) {
        this.sold = sold;
    }

    public Player getBuyer() {
        return buyer;
    }

    public void setBuyer(Player buyer) {
        this.buyer = buyer;
    }

    public int getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(int sellingPrice) {
        this.sellingPrice = sellingPrice;
    }
}

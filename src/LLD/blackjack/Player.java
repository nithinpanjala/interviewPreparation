package LLD.blackjack;

public class Player {
    private String name;
    private Hand hand;

    public Player(String name) { this.name = name; this.hand = new Hand(); }

    public String getName() { return name; }
    public Hand getHand() { return hand; }
    public Boolean isBusted() {
        return this.hand.getScore() > 21;
    }
    public Boolean hasWon() {
        return this.hand.getScore() == 21;
    }
}

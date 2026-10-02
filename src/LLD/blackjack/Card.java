package LLD.blackjack;

public class Card {
    private Rank rank;
    private Suit suit;

    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }

    public Suit getSuit() { return this.suit; }
    public Rank getRank() { return this.rank; }
    public int[] getValues() { return this.rank.values; }

}

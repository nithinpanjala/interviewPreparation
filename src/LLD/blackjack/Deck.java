package LLD.blackjack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    private List<Card> cards;

    public Deck(List<Card> cards) {
        this.cards = cards;
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public Card deal() {
        return cards.removeFirst();
    }
    
    public static Deck initializeStandardDeck() {
        List<Card> cards = new ArrayList<>();
        for(Suit suit: Suit.values()) {
            for(Rank rank: Rank.values()) {
                cards.add(new Card(suit, rank));
            }
        }
        Deck deck = new Deck(cards);
        deck.shuffle();
        return deck;
    }
}

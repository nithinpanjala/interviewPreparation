package LLD.blackjack;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class Hand {
    private List<Card> cards;

    public Hand() {
        this.cards = new ArrayList<>();
    }

    public void addCard(Card card) { this.cards.add(card); }

    public int getScore() {
        return this.calculateScore();
    }

    // A, 3, 3, A, 2, 5
    // 0
    // 1, 11
    // 4, 14
    // 8, 18
    // 9, 19, 19
    // 11, 21, 21

    private int calculateScore() {
        Queue<Integer> scores = new ArrayDeque<>();
        scores.add(0);
        for(Card card : cards) {
            int currentQueueSize = scores.size();
            for(int i = 0; i < currentQueueSize; ++i) {
                Integer current = scores.poll();
                for(int value : card.getValues()) {
                    if(current + value <= 21) {
                        scores.add(current + value);
                    }
                }
            }
            
        }
        return scores.stream().max(Integer::max).get();
    }

    public int calculateScore2() {
        int countAces = 0;
        int score = 0;
        for(Card card: cards) {
            if(Rank.ACE.equals(card.getRank())) {
                ++countAces;
            } else {
                score += card.getRank().values[0];
            }
        }
        for(int i = 0; i <= countAces; ++i) {
            if(score + i * 11 + countAces - i <= 21) {
                score = Math.max(score, score + i * 11 + countAces - i);
            }
        }
        return 0;
    }

  // A, 3, 3, A, 2, 5
       public int calculateScore3() {
        int countAces = 0;
        int score = 0;
        for(Card card: cards) {
            if(Rank.ACE.equals(card.getRank())) {
                ++countAces;
            } 
             score += card.getRank().values[0];
        }
        while(score>21 && countAces>0)
        {
            score = score -10;
            countAces--;
        }
        return score;
    }
}

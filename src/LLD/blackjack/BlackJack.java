package LLD.blackjack;

import java.util.List;
import java.util.Scanner;

public class BlackJack {
    private Deck deck;
    private List<Player> players;
    private Player dealer;
    private Scanner scanner = new Scanner(System.in);

    public BlackJack(Deck deck, List<Player> players, Player dealer) {
        this.deck = deck;
        this.players = players;
        this.dealer = dealer;
    }

    public void start() {
        for(Player player: players) {
            for(int i = 0; i < 2; ++i) {
                Card card = deck.deal();
                player.getHand().addCard(card);
            }
        }
        for(int i = 0; i < 2; ++i) {
            Card card = deck.deal();
            dealer.getHand().addCard(card);
        }

        for(Player player: players) {
            handlePlayerHitOrStand(player);
        }

        handleDealerHitOrStand();

        for(Player player: players) {
            handleStandingPlayer(player);
        }

    }

    public void handlePlayerHitOrStand(Player player) {
        while(true) {
            String choice = scanner.next();
            if("Y".equals(choice)) {
                hit(player);
                if(player.hasWon()) {
                    System.out.printf("Player %s wins\n", player.getName());
                    return;
                }
                if(player.isBusted()) {
                    System.out.printf("Player %s is busted\n", player.getName());
                    return;
                }
            } else {
                System.out.printf("Player %s chose to stand\n", player.getName());
                return;
            }
        }
    }

    public void handleDealerHitOrStand() {
        while(true) {
            String choice = scanner.next();
            if("Y".equals(choice)) {
                hit(dealer);
                if(dealer.hasWon()) {
                    return;
                }
                if(dealer.isBusted()) {
                    System.out.println("Dealer is busted");
                    return;
                }
            } else {
                if(dealer.getHand().getScore() < 17) {
                    System.out.println("Dealer can stand only after having a hand score greater than 17\nDeal another card");
                }
                else{
                    return;
                }
            }
        }
    }

    public void hit(Player player) {
        Card card = deck.deal();
        player.getHand().addCard(card);
    }

    public void handleStandingPlayer(Player player) {
        if(dealer.isBusted()) {
            System.out.printf("Player %s wins\n", player.getName());
            return;
        }
        int dealerScore = dealer.getHand().getScore();
        int playerScore = player.getHand().getScore();
        if(dealerScore > playerScore) {
            System.out.printf("Player %s loses\n", player.getName());
        } else if (dealerScore == playerScore) {
            System.out.printf("Player %s ties\n", player.getName());
        } else {
            System.out.printf("Player %s wins\n", player.getName());
        }
    }
}

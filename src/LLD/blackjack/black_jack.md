
# Blackjack

## Requirements

### Game Setup

- The game supports multiple players playing against a single dealer.
- A standard deck of cards is used to deal cards to players and the dealer.
- At the beginning of the game, each player and the dealer are dealt two cards.
- After the initial deal, additional cards are dealt only when the corresponding player requests a **Hit**.

### Player Actions

- A player can choose between two actions:
  - **Hit** — draw an additional card.
  - **Stand** — stop drawing cards and finalize the current hand.
- A player can choose **Stand** only when their hand value is at least 17.
- The dealer follows the same Hit/Stand rules.
- A player or dealer **busts** when the hand value exceeds 21.
- A player or dealer achieves **Blackjack** when the hand value is exactly 21.

### Scoring

- Each card has an associated value based on its rank.
- Number cards have their face value.
- Face cards (Jack, Queen, and King) have a value of 10.
- An Ace can have a value of either **1 or 11**, depending on which value produces the most favorable valid hand score.
- The hand score should be calculated dynamically based on the cards currently held.

### Determining the Winner

- Once all players have either busted or stood, the dealer's hand is evaluated.
- For every player who has stood:
  - If the dealer busts, the player wins.
  - If the player's score equals the dealer's score, the player's bet is returned (**push**).
  - Otherwise, the player with the higher score wins.
- Players who bust lose their bets regardless of the dealer's final score.
- A hand with a score of 21 is declared **Blackjack**.

## Core Entities

### Card

Represents an individual playing card.

**Attributes:**

- `Suit`
- `Rank`
- `Value`

### Hand

Represents the cards held by a player or dealer.

**Attributes:**

- `List<Card> cards`

**Responsibilities:**

- Add and remove cards.
- Calculate the current hand score.
- Handle Ace valuation as either 1 or 11.

### Deck

Represents the deck of cards from which cards are dealt.

**Attributes:**

- `List<Card> cards`

**Responsibilities:**

- Initialize the deck.
- Shuffle the deck.
- Deal/draw a card.

### Player

Represents a participant in the game, including the dealer.

**Attributes:**

- `name`
- `hand`

**Responsibilities:**

- Calculate the hand score.
- Request a Hit or Stand.
- Maintain the state of the player's hand.

### Blackjack

Represents and manages the overall game.

**Attributes:**

- `Deck deck`
- `List<Player> players`
- `Player dealer`

**Responsibilities:**

- Initialize and start the game.
- Deal the initial two cards.
- Handle player actions such as Hit and Stand.
- Handle dealer actions.
- Detect busts and Blackjacks.
- Determine the outcome for each player.
- Handle standing players and settle their bets.
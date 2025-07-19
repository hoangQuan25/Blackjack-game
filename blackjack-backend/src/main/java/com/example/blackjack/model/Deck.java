package com.example.blackjack.model;

import java.util.Collections;
import java.util.Stack;

import lombok.Data;
import java.io.Serializable;

@Data
public class Deck implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Stack<Card> cards = new Stack<>();

    public Deck(int numberOfDecks) {
        for (int i = 0; i < numberOfDecks; i++) {
            for (Suit suit : Suit.values()) {
                for (Rank rank : Rank.values()) {
                    cards.push(new Card(suit, rank));
                }
            }
        }
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public Card deal() {
        return cards.isEmpty() ? null : cards.pop();
    }
    
    public int size() {
        return cards.size();
    }
}
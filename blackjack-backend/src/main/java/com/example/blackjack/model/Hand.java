package com.example.blackjack.model;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class Hand {
    private List<Card> cards = new ArrayList<>();
    private double betAmount;
    private HandStatus status = HandStatus.PLAYING;

    public void addCard(Card card) {
        cards.add(card);
    }

    public int getHandValue() {
        int value = 0;
        int aceCount = 0;
        for (Card card : cards) {
            value += card.getValue();
            if (card.getRank() == Rank.ACE) {
                aceCount++;
            }
        }
        // Xử lý Át: nếu tổng điểm > 21, chuyển giá trị Át từ 11 về 1
        while (value > 21 && aceCount > 0) {
            value -= 10;
            aceCount--;
        }
        return value;
    }
    
    public void clear() {
        cards.clear();
        betAmount = 0;
    }
}
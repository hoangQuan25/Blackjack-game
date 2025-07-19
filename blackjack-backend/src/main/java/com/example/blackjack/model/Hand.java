package com.example.blackjack.model;

import lombok.Data;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class Hand implements Serializable {
    private static final long serialVersionUID = 1L;

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

    public boolean isBlackjack() {
        return getCards().size() == 2 && getHandValue() == 21;
    }

    public void clear() {
        cards.clear();
        betAmount = 0;
    }
}
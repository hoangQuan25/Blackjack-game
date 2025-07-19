package com.example.blackjack.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import java.io.Serializable;

@Data // Lombok: tự tạo getter, setter, toString...
@RequiredArgsConstructor // Lombok: tự tạo constructor cho các field final
public class Card implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Suit suit;
    private final Rank rank;

    public int getValue() {
        return rank.getValue();
    }
}
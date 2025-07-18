package com.example.blackjack.model;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class GameState {
    private Deck deck;
    private List<Hand> playerHands = new ArrayList<>();
    private Hand dealerHand = new Hand();
    
    private double playerBalance = 1000.0; // Số dư khởi tạo
    
    private String gameMessage;
    private boolean roundOver;
    
    // Danh sách các hành động hợp lệ cho frontend hiển thị
    private List<String> availableActions = new ArrayList<>();
}
package com.example.blackjack.model;

public enum HandStatus {
    PLAYING, // Đang chơi
    STOOD,   // Đã dừng
    BUSTED,  // Đã thua (quắc)
    BLACKJACK; // Thắng Blackjack
}
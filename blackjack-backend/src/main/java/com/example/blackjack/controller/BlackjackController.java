package com.example.blackjack.controller;

import com.example.blackjack.model.GameState;
import com.example.blackjack.service.BlackjackService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game")
public class BlackjackController {

    @Autowired
    private BlackjackService blackjackService;

    // Lấy trạng thái game, hoặc bắt đầu game mới nếu chưa có
    @GetMapping("/state")
    public ResponseEntity<GameState> getGameState(HttpSession session) {
        GameState gameState = (GameState) session.getAttribute("gameState");
        if (gameState == null) {
            gameState = blackjackService.startGame();
            session.setAttribute("gameState", gameState);
        }
        return ResponseEntity.ok(gameState);
    }

    // Đặt cược và bắt đầu ván mới
    @PostMapping("/bet")
    public ResponseEntity<GameState> placeBet(@RequestParam double amount, HttpSession session) {
        GameState gameState = (GameState) session.getAttribute("gameState");
        // Luôn có thể đặt cược để bắt đầu ván mới
        if (gameState == null) {
             gameState = blackjackService.startGame();
        }
        blackjackService.placeBet(gameState, amount);
        session.setAttribute("gameState", gameState);
        return ResponseEntity.ok(gameState);
    }
    
    // Xử lý cược bảo hiểm
    @PostMapping("/insurance")
    public ResponseEntity<GameState> insurance(@RequestParam boolean buy, HttpSession session) {
        GameState gameState = (GameState) session.getAttribute("gameState");
        if (gameState != null) {
            blackjackService.resolveInsurance(gameState, buy);
            session.setAttribute("gameState", gameState);
        }
        return ResponseEntity.ok(gameState);
    }

    // Các hành động của người chơi
    @PostMapping("/hit")
    public ResponseEntity<GameState> hit(@RequestParam int handIndex, HttpSession session) {
        GameState gameState = (GameState) session.getAttribute("gameState");
        if (gameState != null && !gameState.isRoundOver()) {
            blackjackService.playerHit(gameState, handIndex);
            session.setAttribute("gameState", gameState);
        }
        return ResponseEntity.ok(gameState);
    }

    @PostMapping("/stand")
    public ResponseEntity<GameState> stand(@RequestParam int handIndex, HttpSession session) {
        GameState gameState = (GameState) session.getAttribute("gameState");
        if (gameState != null && !gameState.isRoundOver()) {
            blackjackService.playerStand(gameState, handIndex);
            session.setAttribute("gameState", gameState);
        }
        return ResponseEntity.ok(gameState);
    }
    
    @PostMapping("/double")
    public ResponseEntity<GameState> doubleDown(@RequestParam int handIndex, HttpSession session) {
        GameState gameState = (GameState) session.getAttribute("gameState");
        if (gameState != null && !gameState.isRoundOver()) {
            blackjackService.playerDoubleDown(gameState, handIndex);
            session.setAttribute("gameState", gameState);
        }
        return ResponseEntity.ok(gameState);
    }

    @PostMapping("/split")
    public ResponseEntity<GameState> split(@RequestParam int handIndex, HttpSession session) {
        GameState gameState = (GameState) session.getAttribute("gameState");
        if (gameState != null && !gameState.isRoundOver()) {
            blackjackService.playerSplit(gameState, handIndex);
            session.setAttribute("gameState", gameState);
        }
        return ResponseEntity.ok(gameState);
    }
}
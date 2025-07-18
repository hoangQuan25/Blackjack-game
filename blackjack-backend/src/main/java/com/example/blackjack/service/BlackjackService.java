package com.example.blackjack.service;

import com.example.blackjack.model.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BlackjackService {

    private static final int NUM_OF_DECKS = 8;
    private static final int DEALER_STAND_VALUE = 17;

    public GameState startGame() {
        // Chỉ khởi tạo game state với số dư ban đầu
        GameState gameState = new GameState();
        gameState.setGameMessage("Chào mừng đến với Blackjack! Hãy đặt cược.");
        return gameState;
    }

    public void placeBet(GameState gameState, double betAmount) {
        if (betAmount <= 0 || betAmount > gameState.getPlayerBalance()) {
            gameState.setGameMessage("Số tiền cược không hợp lệ.");
            return;
        }

        // Setup ván bài
        gameState.setDeck(new Deck(NUM_OF_DECKS));
        gameState.getDeck().shuffle();
        gameState.getPlayerHands().clear();
        gameState.getDealerHand().clear();
        gameState.setPlayerBalance(gameState.getPlayerBalance() - betAmount);
        
        Hand playerHand = new Hand();
        playerHand.setBetAmount(betAmount);
        playerHand.addCard(gameState.getDeck().deal());
        playerHand.addCard(gameState.getDeck().deal());
        gameState.getPlayerHands().add(playerHand);

        Hand dealerHand = gameState.getDealerHand();
        dealerHand.addCard(gameState.getDeck().deal());
        dealerHand.addCard(gameState.getDeck().deal());
        
        gameState.setRoundOver(false);

        // **LOGIC BẢO HIỂM BẮT ĐẦU TỪ ĐÂY**
        // Nếu lá ngửa của nhà cái là Át, đề nghị bảo hiểm
        if (dealerHand.getCards().get(0).getRank() == Rank.ACE) {
            double insuranceCost = betAmount / 2;
            if (gameState.getPlayerBalance() >= insuranceCost) {
                gameState.setGameMessage("Nhà cái có Át! Bạn muốn mua bảo hiểm (giá: " + insuranceCost + ") không?");
                gameState.setAvailableActions(List.of("BUY_INSURANCE", "NO_INSURANCE"));
                return; // Dừng lại chờ quyết định của người chơi
            }
        }
        
        // Nếu không có Át, tiếp tục như bình thường
        checkInitialBlackjacks(gameState);
    }

    public void resolveInsurance(GameState gameState, boolean playerBuysInsurance) {
        Hand dealerHand = gameState.getDealerHand();
        Hand playerHand = gameState.getPlayerHands().get(0);
        double bet = playerHand.getBetAmount();

        if (playerBuysInsurance) {
            double insuranceCost = bet / 2;
            gameState.setPlayerBalance(gameState.getPlayerBalance() - insuranceCost);
        }

        // Kiểm tra xem nhà cái có Blackjack không
        boolean dealerHasBlackjack = dealerHand.getHandValue() == 21;

        if (dealerHasBlackjack) {
            if (playerBuysInsurance) {
                gameState.setGameMessage("Nhà cái có Blackjack. Bạn thắng cược bảo hiểm!");
                // Thắng bảo hiểm 2:1, nhận lại đúng số tiền cược ban đầu
                gameState.setPlayerBalance(gameState.getPlayerBalance() + bet); 
            } else {
                gameState.setGameMessage("Nhà cái có Blackjack. Bạn thua.");
            }
            gameState.setRoundOver(true);
            gameState.setAvailableActions(List.of("PLACE_BET"));
        } else {
            gameState.setGameMessage("Nhà cái không có Blackjack. Cược bảo hiểm thua. Lượt của bạn.");
            // Game tiếp tục, kiểm tra blackjack của người chơi (trường hợp hiếm)
            checkInitialBlackjacks(gameState);
        }
    }

    private void checkInitialBlackjacks(GameState gameState) {
        Hand playerHand = gameState.getPlayerHands().get(0);
        boolean playerBlackjack = playerHand.getHandValue() == 21;
        
        if (playerBlackjack) {
            resolveBlackjacks(gameState, true, false);
        } else {
            updateAvailableActions(gameState, 0);
        }
    }

    public void playerHit(GameState gameState, int handIndex) {
        Hand currentHand = gameState.getPlayerHands().get(handIndex);
        currentHand.addCard(gameState.getDeck().deal());

        if (currentHand.getHandValue() > 21) {
            currentHand.setStatus(HandStatus.BUSTED);
            gameState.setGameMessage("Bạn đã quắc (bust) ở tay bài " + (handIndex + 1) + "!");
            checkIfPlayerTurnIsOver(gameState);
        } else {
            updateAvailableActions(gameState, handIndex);
        }
    }

    public void playerStand(GameState gameState, int handIndex) {
        Hand currentHand = gameState.getPlayerHands().get(handIndex);
        currentHand.setStatus(HandStatus.STOOD);
        gameState.setGameMessage("Bạn đã dừng ở tay bài " + (handIndex + 1) + ".");
        checkIfPlayerTurnIsOver(gameState);
    }

    public void playerDoubleDown(GameState gameState, int handIndex) {
        Hand currentHand = gameState.getPlayerHands().get(handIndex);
        double betAmount = currentHand.getBetAmount();

        if (gameState.getPlayerBalance() < betAmount) {
             gameState.setGameMessage("Không đủ tiền để cược gấp đôi!");
             return;
        }

        gameState.setPlayerBalance(gameState.getPlayerBalance() - betAmount);
        currentHand.setBetAmount(betAmount * 2);
        currentHand.addCard(gameState.getDeck().deal());

        if (currentHand.getHandValue() > 21) {
            currentHand.setStatus(HandStatus.BUSTED);
        } else {
            currentHand.setStatus(HandStatus.STOOD);
        }
        
        checkIfPlayerTurnIsOver(gameState);
    }
    
    public void playerSplit(GameState gameState, int handIndex) {
        Hand originalHand = gameState.getPlayerHands().get(handIndex);
        double betAmount = originalHand.getBetAmount();

        if (gameState.getPlayerBalance() < betAmount) {
            gameState.setGameMessage("Không đủ tiền để tách bài!");
            return;
        }

        // Tạo tay bài mới
        Hand newHand = new Hand();
        newHand.setBetAmount(betAmount);
        gameState.setPlayerBalance(gameState.getPlayerBalance() - betAmount);

        // Chuyển 1 lá bài và chia thêm bài
        newHand.addCard(originalHand.getCards().remove(1));
        originalHand.addCard(gameState.getDeck().deal());
        newHand.addCard(gameState.getDeck().deal());

        gameState.getPlayerHands().add(handIndex + 1, newHand);
        updateAvailableActions(gameState, handIndex);
    }


    private void checkIfPlayerTurnIsOver(GameState gameState) {
        // Tìm xem còn tay bài nào đang chơi không
        for (Hand hand : gameState.getPlayerHands()) {
            if (hand.getStatus() == HandStatus.PLAYING) {
                // Vẫn còn tay bài đang chơi, cập nhật hành động và chờ
                updateAvailableActionsForCurrentHand(gameState);
                return;
            }
        }
        // Nếu không còn tay bài nào đang chơi -> đến lượt nhà cái
        dealerTurn(gameState);
    }
    
    private void dealerTurn(GameState gameState) {
        gameState.setGameMessage("Lượt của nhà cái.");
        Hand dealerHand = gameState.getDealerHand();
        
        // Lật bài và rút cho đến khi đủ hoặc hơn 17
        while (dealerHand.getHandValue() < DEALER_STAND_VALUE) {
            dealerHand.addCard(gameState.getDeck().deal());
        }
        
        resolveBets(gameState);
    }

    private void resolveBets(GameState gameState) {
        Hand dealerHand = gameState.getDealerHand();
        int dealerValue = dealerHand.getHandValue();
        StringBuilder finalMessage = new StringBuilder("Kết quả: ");

        for (Hand playerHand : gameState.getPlayerHands()) {
            int playerValue = playerHand.getHandValue();
            double bet = playerHand.getBetAmount();
            
            if (playerHand.getStatus() == HandStatus.BUSTED) {
                finalMessage.append("Bạn thua (quắc). ");
                // Tiền đã bị trừ
            } else if (dealerValue > 21 || playerValue > dealerValue) {
                finalMessage.append("Bạn thắng! ");
                gameState.setPlayerBalance(gameState.getPlayerBalance() + bet * 2);
            } else if (playerValue < dealerValue) {
                finalMessage.append("Bạn thua. ");
            } else { // push
                finalMessage.append("Hòa (push). ");
                gameState.setPlayerBalance(gameState.getPlayerBalance() + bet);
            }
        }
        gameState.setGameMessage(finalMessage.toString());
        gameState.setRoundOver(true);
        gameState.setAvailableActions(List.of("PLACE_BET")); // Chỉ cho phép cược lại
    }

    private void resolveBlackjacks(GameState gameState, boolean playerBlackjack, boolean dealerBlackjack) {
        Hand playerHand = gameState.getPlayerHands().get(0);
        double bet = playerHand.getBetAmount();

        if (playerBlackjack && !dealerBlackjack) {
            gameState.setGameMessage("BLACKJACK! Bạn thắng 3:2!");
            gameState.setPlayerBalance(gameState.getPlayerBalance() + bet * 2.5); // Cược gốc + 1.5 lần tiền thắng
        } else if (!playerBlackjack && dealerBlackjack) {
            gameState.setGameMessage("Nhà cái có Blackjack. Bạn thua.");
        } else { // cả hai cùng có
            gameState.setGameMessage("Hòa Blackjack (Push).");
            gameState.setPlayerBalance(gameState.getPlayerBalance() + bet);
        }
        gameState.setRoundOver(true);
        gameState.setAvailableActions(List.of("PLACE_BET"));
    }
    
    private void updateAvailableActionsForCurrentHand(GameState gameState) {
        for(int i = 0; i < gameState.getPlayerHands().size(); i++){
            if(gameState.getPlayerHands().get(i).getStatus() == HandStatus.PLAYING){
                updateAvailableActions(gameState, i);
                return;
            }
        }
    }

    private void updateAvailableActions(GameState gameState, int handIndex) {
        List<String> actions = new ArrayList<>();
        Hand currentHand = gameState.getPlayerHands().get(handIndex);
        
        actions.add("HIT");
        actions.add("STAND");
        
        // Kiểm tra điều kiện Double Down
        if (currentHand.getCards().size() == 2 && gameState.getPlayerBalance() >= currentHand.getBetAmount()) {
            actions.add("DOUBLE_DOWN");
        }

        // Kiểm tra điều kiện Split
        if (currentHand.getCards().size() == 2 &&
            currentHand.getCards().get(0).getRank() == currentHand.getCards().get(1).getRank() &&
            gameState.getPlayerBalance() >= currentHand.getBetAmount()) {
            actions.add("SPLIT");
        }
        
        gameState.setAvailableActions(actions);
    }
}
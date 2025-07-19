package com.example.blackjack.service;

import com.example.blackjack.model.*;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class BlackjackService {

    private static final int NUM_OF_DECKS = 8;
    private static final int DEALER_STAND_VALUE = 17;

    public GameState startGame() {
        // Chỉ khởi tạo game state với số dư ban đầu
        GameState gameState = new GameState();
        gameState.setGameMessage("Chào mừng đến với Blackjack! Hãy đặt cược.");
        gameState.setAvailableActions(List.of("PLACE_BET"));

        return gameState;
    }

    public GameState placeBet(GameState gameState, double betAmount) {
        if (betAmount <= 0 || betAmount > gameState.getPlayerBalance()) {
            gameState.setGameMessage("Số tiền cược không hợp lệ.");
            return gameState;
        }

        double reshuffleThreshold = NUM_OF_DECKS * 52 * 0.4;

        if (gameState.getDeck() == null || gameState.getDeck().size() < reshuffleThreshold) {
            log.info("Bài còn lại quá ít, xáo lại bộ bài mới...");
            gameState.setDeck(new Deck(NUM_OF_DECKS));
            gameState.getDeck().shuffle();
            // Bạn có thể thêm một thông báo cho người chơi biết
            gameState.setGameMessage("Bộ bài đã được xáo lại. ");
        }

        // Setup ván bài
        // gameState.setDeck(new Deck(NUM_OF_DECKS));
        // gameState.getDeck().shuffle();
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

        String currentMessage = gameState.getGameMessage() != null && gameState.getGameMessage().contains("xáo lại")
                ? gameState.getGameMessage()
                : "";
        gameState.setGameMessage(currentMessage + "Ván bài bắt đầu!");

        // **LOGIC BẢO HIỂM BẮT ĐẦU TỪ ĐÂY**
        // Nếu lá ngửa của nhà cái là Át, đề nghị bảo hiểm
        if (dealerHand.getCards().get(0).getRank() == Rank.ACE) {
            double insuranceCost = betAmount / 2;
            if (gameState.getPlayerBalance() >= insuranceCost) {
                gameState.setGameMessage("Nhà cái có Át! Bạn muốn mua bảo hiểm (giá: " + insuranceCost + ") không?");
                gameState.setAvailableActions(List.of("BUY_INSURANCE", "NO_INSURANCE"));
                return gameState; // Dừng lại chờ quyết định của người chơi
            }
        }

        // Nếu không có Át, tiếp tục như bình thường
        checkInitialBlackjacks(gameState);
        return gameState;
    }

    public GameState resolveInsurance(GameState gameState, boolean playerBuysInsurance) {
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
            // Xử lý tiền cược bảo hiểm trước
            if (playerBuysInsurance) {
                gameState.setGameMessage("Nhà cái có Blackjack. Bạn thắng cược bảo hiểm! ");
                gameState.setPlayerBalance(gameState.getPlayerBalance() + playerHand.getBetAmount());
            } else {
                gameState.setGameMessage("Nhà cái có Blackjack. Cược bảo hiểm thua. ");
            }

            // Bây giờ kiểm tra ván cược chính
            if (playerHand.isBlackjack()) {
                gameState.setGameMessage(gameState.getGameMessage() + "Ván cược chính hòa (Push).");
                gameState.setPlayerBalance(gameState.getPlayerBalance() + playerHand.getBetAmount());
            } else {
                gameState.setGameMessage(gameState.getGameMessage() + "Bạn thua ván cược chính.");
            }

            gameState.setRoundOver(true);
            gameState.setAvailableActions(List.of("PLACE_BET"));
        } else {
            if (playerBuysInsurance) {
                // Nếu có mua, thông báo cược bảo hiểm thua
                gameState.setGameMessage("Nhà cái không có Blackjack. Cược bảo hiểm thua. Lượt của bạn.");
            } else {
                // Nếu không mua, chỉ cần thông báo và tiếp tục
                gameState.setGameMessage("Nhà cái không có Blackjack. Lượt của bạn.");
            }
            checkInitialBlackjacks(gameState);
        }

        return gameState;
    }

    private void checkInitialBlackjacks(GameState gameState) {
        Hand playerHand = gameState.getPlayerHands().get(0);
        Hand dealerHand = gameState.getDealerHand();

        boolean playerHasBlackjack = playerHand.isBlackjack();
        boolean dealerHasBlackjack = dealerHand.isBlackjack();

        // Chỉ xử lý khi có ít nhất một bên có Blackjack
        if (playerHasBlackjack || dealerHasBlackjack) {
            resolveBlackjacks(gameState, playerHasBlackjack, dealerHasBlackjack);
        } else {
            // Nếu không ai có Blackjack, cập nhật hành động cho người chơi
            updateAvailableActions(gameState, 0);
        }
    }

    public GameState playerHit(GameState gameState, int handIndex) {
        log.info("Entering playerHit with gameState: {}", gameState); // Log khi bắt đầu
        if (gameState == null || gameState.getDeck() == null) {
            log.error("GameState or Deck is null at the start of playerHit!");
            return null; // Trả về null nếu có lỗi đầu vào
        }

        Hand currentHand = gameState.getPlayerHands().get(handIndex);
        currentHand.addCard(gameState.getDeck().deal());

        if (currentHand.getHandValue() > 21) {
            currentHand.setStatus(HandStatus.BUSTED);
            gameState.setGameMessage("Bạn đã quắc (bust) ở tay bài " + (handIndex + 1) + "!");
            checkIfPlayerTurnIsOver(gameState);
        } else {
            updateAvailableActions(gameState, handIndex);
        }

        log.info("Exiting playerHit with gameState: {}", gameState);
        return gameState;
    }

    public GameState playerStand(GameState gameState, int handIndex) {
        Hand currentHand = gameState.getPlayerHands().get(handIndex);
        currentHand.setStatus(HandStatus.STOOD);
        gameState.setGameMessage("Bạn đã dừng ở tay bài " + (handIndex + 1) + ".");
        checkIfPlayerTurnIsOver(gameState);
        return gameState;
    }

    public GameState playerDoubleDown(GameState gameState, int handIndex) {
        Hand currentHand = gameState.getPlayerHands().get(handIndex);
        double betAmount = currentHand.getBetAmount();

        if (gameState.getPlayerBalance() < betAmount) {
            gameState.setGameMessage("Không đủ tiền để cược gấp đôi!");
            return gameState;
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

        return gameState;
    }

    public GameState playerSplit(GameState gameState, int handIndex) {
        Hand originalHand = gameState.getPlayerHands().get(handIndex);
        double betAmount = originalHand.getBetAmount();

        if (gameState.getPlayerBalance() < betAmount) {
            gameState.setGameMessage("Không đủ tiền để tách bài!");
            return gameState;
        }

        // Lấy rank của lá bài trước khi tách để kiểm tra có phải là Át không
        Rank splitRank = originalHand.getCards().get(0).getRank();

        // Setup tay bài mới và trừ tiền
        gameState.setPlayerBalance(gameState.getPlayerBalance() - betAmount);
        Hand newHand = new Hand();
        newHand.setBetAmount(betAmount);

        // Chuyển 1 lá bài và chia thêm bài cho cả hai tay
        newHand.addCard(originalHand.getCards().remove(1));
        originalHand.addCard(gameState.getDeck().deal());
        newHand.addCard(gameState.getDeck().deal());
        gameState.getPlayerHands().add(handIndex + 1, newHand);

        // KIỂM TRA LUẬT ĐẶC BIỆT KHI TÁCH ÁT
        if (splitRank == Rank.ACE) {
            // Tự động cho cả hai tay "Dừng" (Stand) sau khi nhận thêm 1 lá
            originalHand.setStatus(HandStatus.STOOD);
            newHand.setStatus(HandStatus.STOOD);
            gameState.setGameMessage("Bạn đã tách Át. Mỗi tay nhận thêm một lá và lượt chơi của hai tay này kết thúc.");

            // Kiểm tra xem lượt của người chơi đã hết chưa (để chuyển cho nhà cái)
            checkIfPlayerTurnIsOver(gameState);
        } else {
            // Nếu không phải tách Át, thì tiếp tục như bình thường
            gameState.setGameMessage("Bạn đã tách bài. Chơi tay bài đầu tiên.");
            updateAvailableActions(gameState, handIndex);
        }

        return gameState;
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
        // Khởi tạo StringBuilder
        StringBuilder finalMessage = new StringBuilder("Kết quả - ");
        int handNumber = 1; // Dùng để đánh số thứ tự tay bài

        for (Hand playerHand : gameState.getPlayerHands()) {
            int playerValue = playerHand.getHandValue();
            double bet = playerHand.getBetAmount();

            // Thêm định danh cho mỗi tay bài vào tin nhắn
            if (gameState.getPlayerHands().size() > 1) {
                finalMessage.append("Tay ").append(handNumber).append(": ");
            }

            if (playerHand.getStatus() == HandStatus.BUSTED) {
                finalMessage.append("Thua (quắc). ");
                // Tiền đã bị trừ
            } else if (dealerValue > 21 || playerValue > dealerValue) {
                finalMessage.append("Thắng! ");
                gameState.setPlayerBalance(gameState.getPlayerBalance() + bet * 2);
            } else if (playerValue < dealerValue) {
                finalMessage.append("Thua. ");
            } else { // push
                finalMessage.append("Hòa (push). ");
                gameState.setPlayerBalance(gameState.getPlayerBalance() + bet);
            }
            handNumber++; // Tăng số đếm cho tay bài tiếp theo
        }

        // Đặt thông báo cuối cùng đã được định dạng
        gameState.setGameMessage(finalMessage.toString().trim()); // .trim() để xóa dấu cách thừa ở cuối
        gameState.setRoundOver(true);
        gameState.setAvailableActions(List.of("PLACE_BET"));
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
        for (int i = 0; i < gameState.getPlayerHands().size(); i++) {
            if (gameState.getPlayerHands().get(i).getStatus() == HandStatus.PLAYING) {
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
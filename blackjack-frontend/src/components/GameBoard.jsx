import { useState, useEffect, useRef } from "react";
import Hand from "./Hand";

const GameBoard = ({ gameState }) => {
  const [displayedState, setDisplayedState] = useState(gameState);
  const prevGameStateRef = useRef();

  useEffect(() => {
    const prev = prevGameStateRef.current;
    if (!prev) {
      // Bỏ qua lần render đầu tiên
      prevGameStateRef.current = gameState;
      return;
    }

    const masterDelay = 600; // Tăng delay một chút để kịp nhìn

    // --- KỊCH BẢN 1: BẮT ĐẦU MỘT VÁN MỚI ---
    // Dấu hiệu đơn giản: Ván bài trước đó đã kết thúc.
    if (prev.roundOver) {
      // Bước A: Hiển thị các lá bài mới của ván này ngay lập tức.
      // Ta tạm thời giả định ván bài chưa kết thúc để các lá bài hiện ra.
      const intermediateState = {
        ...gameState,
        roundOver: false, // Tạm thời đặt là false để hiện bài
        gameMessage: "Đang chia bài...",
      };
      setDisplayedState(intermediateState);

      // Bước B: Nếu ván mới này kết thúc ngay (trường hợp Blackjack),
      // thì sau một khoảng trễ, cập nhật state cuối cùng để hiện thông báo thắng.
      if (gameState.roundOver) {
        setTimeout(() => {
          setDisplayedState(gameState);
        }, masterDelay);
      }

      prevGameStateRef.current = gameState;
      return;
    }

    // --- KỊCH BẢN 2: VÁN BÀI VỪA KẾT THÚC (TRONG MỘT VÁN ĐANG DIỄN RA) ---
    // Dấu hiệu: Ván trước đang chơi VÀ ván này đã kết thúc.
    if (!prev.roundOver && gameState.roundOver) {
      let currentState = JSON.parse(JSON.stringify(displayedState));
      let sequenceDelay = 0;

      // Lật bài của nhà cái
      setTimeout(() => {
        currentState.playerHands = gameState.playerHands;
        setDisplayedState(JSON.parse(JSON.stringify(currentState)));
      }, sequenceDelay);

      // Nhà cái rút thêm bài...
      const initialDealerCards = displayedState.dealerHand.cards.length;
      for (
        let i = initialDealerCards;
        i < gameState.dealerHand.cards.length;
        i++
      ) {
        sequenceDelay += masterDelay;
        let cardIndex = i;
        setTimeout(() => {
          currentState.dealerHand.cards.push(
            gameState.dealerHand.cards[cardIndex]
          );
          setDisplayedState(JSON.parse(JSON.stringify(currentState)));
        }, sequenceDelay);
      }

      // Hiện kết quả cuối cùng...
      sequenceDelay += masterDelay;
      setTimeout(() => {
        setDisplayedState(gameState);
      }, sequenceDelay);

      prevGameStateRef.current = gameState;
      return;
    }

    // --- KỊCH BẢN 3: NGƯỜI CHƠI RÚT BÀI ---
    if (
      gameState.playerHands.some(
        (hand, i) =>
          hand.cards.length > (prev.playerHands[i]?.cards.length || 0)
      )
    ) {
      setTimeout(() => {
        setDisplayedState(gameState);
      }, masterDelay / 2);
    }

    // Cập nhật ref cho lần render tiếp theo
    prevGameStateRef.current = gameState;
  }, [gameState]);

  if (!displayedState.dealerHand) return null;

  const isPlayerTurn = displayedState.playerHands.some(
    (h) => h.status === "PLAYING"
  );

  return (
    <div className="space-y-8">
      {/* Bài của nhà cái */}
      <div>
        <h2 className="text-2xl font-semibold mb-2">
          Nhà cái (
          {displayedState.roundOver || !isPlayerTurn
            ? displayedState.dealerHand.handValue
            : "?"}
          )
        </h2>
        <Hand
          hand={displayedState.dealerHand}
          isDealer={true}
          hideFirstCard={isPlayerTurn && !displayedState.roundOver}
        />
      </div>

      {/* Bài của người chơi */}
      <div>
        <h2 className="text-2xl font-semibold mb-2">Người chơi</h2>
        <div className="space-y-4">
          {displayedState.playerHands.map((hand, index) => (
            <div
              key={index}
              className={`p-2 rounded transition-all duration-300 ${
                hand.status === "PLAYING"
                  ? "bg-yellow-500 bg-opacity-30 scale-105"
                  : ""
              }`}
            >
              <p className="font-bold">
                Tay {index + 1} (Điểm: {hand.handValue}) - Cược: $
                {hand.betAmount}
              </p>
              <Hand hand={hand} />
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default GameBoard;

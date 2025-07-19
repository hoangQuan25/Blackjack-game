import { useState, useEffect } from 'react';
import GameBoard from './components/GameBoard';
import Actions from './components/Actions';
import * as api from './api/api'; // Chúng ta sẽ tạo file này ngay sau đây

export default function App() {
  const [gameState, setGameState] = useState(null);
  const [loading, setLoading] = useState(true);

  // Lấy trạng thái game khi component được tải lần đầu
  useEffect(() => {
    const fetchGameState = async () => {
      try {
        const state = await api.getGameState();
        setGameState(state);
      } catch (error) {
        console.error("Lỗi khi lấy trạng thái game:", error);
      } finally {
        setLoading(false);
      }
    };
    fetchGameState();
  }, []);

  const handleApiCall = async (apiFunction) => {
    try {
      const newState = await apiFunction();
      setGameState(newState);
    } catch (error) {
      console.error("Lỗi khi thực hiện hành động:", error);
    }
  };
  
  const getActiveHandIndex = () => {
    if (!gameState || !gameState.playerHands) return -1;
    return gameState.playerHands.findIndex(hand => hand.status === 'PLAYING');
  };

  const handleHit = () => {
    const activeIndex = getActiveHandIndex();
    if (activeIndex !== -1) {
      handleApiCall(() => api.hit(activeIndex));
    }
  };

  const handleStand = () => {
    const activeIndex = getActiveHandIndex();
    if (activeIndex !== -1) {
      handleApiCall(() => api.stand(activeIndex));
    }
  };
  
  const handleDouble = () => {
      const activeIndex = getActiveHandIndex();
      if (activeIndex !== -1) {
          handleApiCall(() => api.doubleDown(activeIndex));
      }
  };
  
  const handleSplit = () => {
      const activeIndex = getActiveHandIndex();
      if (activeIndex !== -1) {
          handleApiCall(() => api.split(activeIndex));
      }
  };

  if (loading) {
    return <div className="bg-gray-900 text-white min-h-screen flex items-center justify-center">Đang tải...</div>;
  }

  if (!gameState) {
    return <div className="bg-gray-900 text-white min-h-screen flex items-center justify-center">Không thể kết nối tới server game.</div>;
  }

  return (
    <div className="bg-green-800 text-white min-h-screen flex flex-col items-center p-4 font-sans">
      <h1 className="text-4xl font-bold mb-4">Blackjack</h1>
      
      <div className="w-full max-w-4xl">
        <GameBoard gameState={gameState} />
        
        <div className="my-4 p-3 bg-black bg-opacity-50 rounded-lg text-center">
            <p className="text-xl font-semibold">Số dư: ${gameState.playerBalance.toFixed(2)}</p>
            <p className="text-lg italic mt-1">{gameState.gameMessage}</p>
        </div>

        <Actions 
          gameState={gameState} 
          handlers={{
            handleBet: (amount) => handleApiCall(() => api.placeBet(amount)),
            handleInsurance: (buy) => handleApiCall(() => api.resolveInsurance(buy)),
            handleHit: handleHit,
            handleStand: handleStand,
            handleDouble: handleDouble,
            handleSplit: handleSplit
          }}
        />
      </div>
    </div>
  );
}
import { useState } from 'react';

const ActionButton = ({ children, onClick }) => (
  <button
    onClick={onClick}
    className="bg-yellow-500 hover:bg-yellow-600 text-gray-900 font-bold py-2 px-4 rounded-lg shadow-md transition duration-200"
  >
    {children}
  </button>
);

const Actions = ({ gameState, handlers }) => {
  const { availableActions } = gameState;
  const [betAmount, setBetAmount] = useState(50);

  if (availableActions.includes('PLACE_BET')) {
    return (
      <div className="flex items-center justify-center space-x-4">
        <input 
            type="number"
            value={betAmount}
            onChange={(e) => setBetAmount(Number(e.target.value))}
            className="w-32 p-2 rounded bg-gray-900 text-white border border-gray-600"
            step="10"
            min="10"
        />
        <ActionButton onClick={() => handlers.handleBet(betAmount)}>Đặt Cược</ActionButton>
      </div>
    );
  }
  
  if (availableActions.includes('BUY_INSURANCE')) {
    return (
        <div className="flex items-center justify-center space-x-4">
            <ActionButton onClick={() => handlers.handleInsurance(true)}>Mua Bảo hiểm</ActionButton>
            <ActionButton onClick={() => handlers.handleInsurance(false)}>Không, cảm ơn</ActionButton>
        </div>
    )
  }

  return (
    <div className="flex items-center justify-center space-x-4">
      {availableActions.includes('HIT') && <ActionButton onClick={handlers.handleHit}>Rút (Hit)</ActionButton>}
      {availableActions.includes('STAND') && <ActionButton onClick={handlers.handleStand}>Dừng (Stand)</ActionButton>}
      {availableActions.includes('DOUBLE_DOWN') && <ActionButton onClick={handlers.handleDouble}>Cược Gấp Đôi</ActionButton>}
      {availableActions.includes('SPLIT') && <ActionButton onClick={handlers.handleSplit}>Tách Bài</ActionButton>}
    </div>
  );
};

export default Actions;
const Card = ({ card, isNew = false }) => {

  const animationClass = isNew ? 'animate-deal-in' : '';
  // Render lá bài úp
  if (!card) {
    return (
      <div className="w-24 h-32 bg-blue-600 rounded-lg border-2 border-white flex items-center justify-center">
        <span className="text-5xl text-blue-300">?</span>
      </div>
    );
  }

  const { suit, rank } = card;
  const isRed = suit.icon === '♥' || suit.icon === '♦';
  const colorClass = isRed ? 'text-red-600' : 'text-black';

  return (
    <div className={`w-24 h-32 bg-white rounded-lg border-2 border-gray-400 shadow-lg p-2 flex flex-col justify-between transition-transform duration-300 ${animationClass}`}>
      <span className={`text-3xl font-bold ${colorClass}`}>{rank.display}</span>
      <span className={`text-4xl self-center ${colorClass}`}>{suit.icon}</span>
    </div>
  );
};

export default Card;
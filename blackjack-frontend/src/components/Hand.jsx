import Card from './Card';

const Hand = ({ hand, isDealer = false, hideFirstCard = false }) => {
  return (
    <div className="flex space-x-2 h-36 items-center">
      {hand.cards.map((card, index) => {
        const shouldHide = isDealer && hideFirstCard && index === 1;
        return <Card key={index} card={shouldHide ? null : card} />;
      })}
    </div>
  );
};

export default Hand;
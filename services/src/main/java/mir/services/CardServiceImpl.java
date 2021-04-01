package mir.services;

import mir.models.Card;
import mir.repositories.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.Table;
import javax.transaction.Transactional;

@Service
public class CardServiceImpl implements CardService {

    private final CardRepository repository;

    @Autowired
    public CardServiceImpl(CardRepository repository) {
        this.repository = repository;
    }

    @Override
    public Card registerCard(Card card) {
        return repository.save(card);
    }

    @Override
    public boolean existsByNumber(String number) {
        return repository.existsByNumber(number);
    }

    @Override
    @Transactional
    public String transferMoney(Card sender, String recipientCardNumber, Long amountOfMoney) {
        if (!repository.existsByNumber(sender.getNumber()))
            return "Sender card number doesn't exists";

        if (!repository.existsByNumber(recipientCardNumber))
            return "Recipient card number doesn't exists";

        var currentSender = repository.findByNumber(sender.getNumber());
        var currentRecipient = repository.findByNumber(recipientCardNumber);

        currentSender.setMoney(currentSender.getMoney() - amountOfMoney);
        currentRecipient.setMoney(currentRecipient.getMoney() + amountOfMoney);

        return "Money transferred successfully";
    }

    @Override
    @Transactional
    public Long depositMoney(String cardNumber, Long money) {
        Card card = repository.findByNumber(cardNumber);
        card.setMoney(card.getMoney() + money);
        return money;
    }

    @Override
    @Transactional
    public Long writeOffMoney(String cardNumber, Long money) throws IllegalStateException{
        Card card = repository.findByNumber(cardNumber);
        long currentMoney = card.getMoney() - money;
        if (currentMoney < 0)
            throw new IllegalStateException("Not enough money to write-off");

        card.setMoney(currentMoney);
        return money;
    }
}

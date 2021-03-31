package mir.services;

import mir.models.Card;
import mir.repositories.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class CardServiceImpl implements CardService {

    private CardRepository repository;

    @Autowired
    public CardServiceImpl(CardRepository repository) {
        this.repository = repository;
    }

    @Override
    public String registerCard(Card card) {
        repository.save(card);

        return "Card registered";
    }

    @Override
    @Transactional
    public String transferMoney(Card sender, String recipientCardNumber, Double amountOfMoney) {
        if(!repository.existsByNumber(sender.getNumber()))
            return "Sender card number doesn't exists";

        if(!repository.existsByNumber(recipientCardNumber))
            return "Recipient card number doesn't exists";

        var currentSender  = repository.findByNumber(sender.getNumber());
        var currentRecipient = repository.findByNumber(recipientCardNumber);

        currentSender.setMoney(currentSender.getMoney() - amountOfMoney);
        currentRecipient.setMoney(currentRecipient.getMoney() + amountOfMoney);

        return "Money transferred successfully";
    }
}

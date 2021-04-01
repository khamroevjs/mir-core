package mir.services;

import mir.models.Card;
import mir.models.ParsedMessage;

public interface CardService {

    Card registerCard(Card card);

    String transferMoney(Card sender, String recipientCardNumber, Long amountOfMoney);
    boolean existsByNumber(String number);

    Long depositMoney(String cardNumber, Long money);
    Long writeOffMoney(String cardNumber, Long money) throws IllegalStateException;
}

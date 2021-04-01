package mir.services;

import mir.models.Card;
import mir.models.ParsedMessage;

public interface CardService {

    Card registerCard(Card card) throws IllegalStateException;

    boolean existsByNumber(String number);

    Long depositMoney(String cardNumber, Long money);
    Long writeOffMoney(String cardNumber, Long money) throws IllegalStateException;
}

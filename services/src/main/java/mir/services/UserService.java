package mir.services;

import mir.repositories.UserRepository;
import org.springframework.stereotype.Service;

public interface UserService {
    boolean existsByCardNumber(String cardNumber);

    Long depositMoney(String cardNumber, Long money);
    Long writeOffMoney(String cardNumber, Long money) throws IllegalStateException;
}

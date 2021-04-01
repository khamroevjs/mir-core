package mir.services;

import mir.models.User;
import mir.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.NumberUtils;

import javax.transaction.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository repository;

    @Autowired
    public UserServiceImpl(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existsByCardNumber(String cardNumber) {
        //cardNumber.replaceFirst("^0+(?!$)", "");
        long currentCardNumber = Long.parseLong(cardNumber);
        return repository.existsByCardNumber(Long.toString(currentCardNumber));
    }

    @Override
    @Transactional
    public Long depositMoney(String cardNumber, Long money) {
        long currentCardNumber = Long.parseLong(cardNumber);
        User user = repository.findByCardNumber(Long.toString(currentCardNumber));
        user.setMoney(user.getMoney() + money);
        return money;
    }

    @Override
    @Transactional
    public Long writeOffMoney(String cardNumber, Long money) throws IllegalStateException {
        long currentCardNumber = Long.parseLong(cardNumber);
        User user = repository.findByCardNumber(Long.toString(currentCardNumber));
        long currentMoney = user.getMoney() - money;
        if (currentMoney < 0)
            throw new IllegalStateException("Not enough money to write-off");

        user.setMoney(currentMoney);
        return money;
    }
}

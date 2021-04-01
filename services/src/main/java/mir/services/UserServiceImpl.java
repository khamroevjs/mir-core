package mir.services;

import mir.models.User;
import mir.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository repository;

    @Autowired
    public UserServiceImpl(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existsByCardNumber(String cardNumber)
    {
        return repository.existsByCardNumber(cardNumber);
    }

    @Override
    @Transactional
    public Long depositMoney(String cardNumber, Long money) {
        User user = repository.findByCardNumber(cardNumber);
        user.setMoney(user.getMoney() + money);
        return money;
    }

    @Override
    @Transactional
    public Long writeOffMoney(String cardNumber, Long money) throws IllegalStateException{
        User user = repository.findByCardNumber(cardNumber);
        long currentMoney = user.getMoney() - money;
        if(currentMoney < 0)
            throw new IllegalStateException("Not enough money to write-off");

        user.setMoney(currentMoney);
        return money;
    }
}

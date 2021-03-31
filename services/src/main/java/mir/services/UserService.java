package mir.services;

import mir.repositories.UserRepository;
import org.springframework.stereotype.Service;

public interface UserService {
    public boolean exists(String cardNumber);
}

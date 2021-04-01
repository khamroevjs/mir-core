package mir.repositories;

import mir.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    boolean existsByCardNumber(String cardNumber);

    User findByCardNumber(String cardNumber);
}

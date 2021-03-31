package mir.repositories;

import mir.models.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardRepository extends JpaRepository<Card, String> {

    boolean existsByNumber(String number);

    Card findByNumber(String number);
}

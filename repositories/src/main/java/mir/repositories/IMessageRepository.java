package mir.repositories;

import mir.models.ParsedMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IMessageRepository extends JpaRepository<ParsedMessage, Integer> {

    /**
     * Returns all messages with given MTI
     *
     * @param mti MTI
     * @return list of messages
     */
    @Query(value = "SELECT * FROM public.parsed_message WHERE mti = :mti",
            nativeQuery = true)
    List<ParsedMessage> findAllByMti(@Param("mti") String mti);

    /**
     * Returns a message with given transaction number
     *
     * @param transactionNumber Transaction number
     * @return message
     */
    @Query(value = "SELECT * FROM public.parsed_message WHERE transaction_number = :transactionNumber",
            nativeQuery = true)
    ParsedMessage findByTransactionNumber(@Param("transactionNumber") String transactionNumber);

    /**
     * Returns all messages in given dateTime range [start, end]
     *
     * @param start Start date
     * @param end   End date
     * @return list of messages
     */
    List<ParsedMessage> findAllByTransactionDateBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Returns all messages with given HEX
     *
     * @param hex HEX
     * @return list of messages
     */
    @Query(value = "SELECT * FROM public.parsed_message WHERE hex = :hex",
            nativeQuery = true)
    List<ParsedMessage> findAllByHex(@Param("hex") String hex);

    /**
     * Returns all messages with given value of "edited"
     *
     * @param edited indicates if messages edited
     * @return list of messages
     */
    @Query(value = "SELECT * FROM public.parsed_message WHERE edited = :edited",
            nativeQuery = true)
    List<ParsedMessage> findAllEdited(@Param("edited") boolean edited);

    /**
     * Deletes all messages with given MTI
     *
     * @param mti MTI
     */
    @Query(value = "DELETE FROM public.parsed_message WHERE mti = :mti",
            nativeQuery = true)
    void deleteAllByMti(@Param("mti") String mti);

    /**
     * Deletes one message with given transaction number
     *
     * @param transactionNumber Transaction number
     */
    @Query(value = "DELETE FROM public.parsed_message WHERE transaction_number = :transactionNumber",
            nativeQuery = true)
    void deleteByTransactionNumber(@Param("transactionNumber") String transactionNumber);

    /**
     * Deletes all messages in given dateTime range [start, end]
     * @param start Start date
     * @param end   End date
     */
    void deleteAllByTransactionDateBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Deletes all messages with given HEX
     * @param hex HEX
     */
    @Query(value = "DELETE FROM public.parsed_message WHERE hex = :hex"
            , nativeQuery = true)
    void deleteAllByHex(@Param("hex") String hex);

    /**
     * Deletes all messages with given value of "edited"
     *
     * @param edited indicates if messages edited
     */
    @Query(value = "SELECT * FROM public.parsed_message WHERE edited = :edited"
            , nativeQuery = true)
    void deleteAllByEdited(@Param("edited") boolean edited);
}

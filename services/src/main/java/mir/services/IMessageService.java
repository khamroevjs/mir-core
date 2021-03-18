package mir.services;

import mir.models.ParsedMessage;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface IMessageService {

    List<ParsedMessage> getAll();

    void add(ParsedMessage parsedMessage);

    void deleteById(Integer id);

    void update(Integer id, ParsedMessage parsedMessage);

    List<ParsedMessage> getAllByMti(String mti);

    ParsedMessage getByTransactionNumber(String transactionNumber);

    List<ParsedMessage> getAllByTransactionDateBetween(LocalDateTime start, LocalDateTime end);

    List<ParsedMessage> getAllByHex(String hex);

    List<ParsedMessage> getAllEdited(boolean edited);

    void deleteAllByMti(String mti);

    void deleteByTransactionNumber(String transactionNumber);

    void deleteAllByTransactionDateBetween(LocalDateTime start, LocalDateTime end);

    void deleteAllByHex(String hex);

    void deleteAllByEdited(boolean edited);
}

package mir.services;

import mir.models.ParsedMessage;
import mir.repositories.IMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService implements IMessageService {

    private final IMessageRepository repository;

    @Autowired
    public MessageService(IMessageRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<ParsedMessage> getAll() {
        return repository.findAll();
    }

    @Override
    public void add(ParsedMessage parsedMessage) {
        parsedMessage.setTransactionDate(LocalDateTime.now());
        repository.save(parsedMessage);
    }

    @Override
    public void deleteById(Integer id) {
        if (!repository.existsById(id))
            throw new IllegalStateException("ParsedMessage with " + id + " doesn't exists");

        repository.deleteById(id);
    }

    @Override
    @Transactional
    public void update(Integer id, ParsedMessage parsedMessage) {

        var message = repository.findById(id).orElseThrow(
                () -> new IllegalStateException("ParsedMessage with " + id + " doesn't exists"));

        message.setMti(parsedMessage.getMti());
        message.setEdited(true);
        message.setTransactionDate(parsedMessage.getTransactionDate());
        message.setTransactionNumber(parsedMessage.getTransactionNumber());
    }

    @Override
    public List<ParsedMessage> getAllByMti(String mti) {
        return repository.findAllByMti(mti);
    }

    @Override
    public ParsedMessage getByTransactionNumber(String transactionNumber) {
        return repository.findByTransactionNumber(transactionNumber);
    }

    @Override
    public List<ParsedMessage> getAllByTransactionDateBetween(LocalDateTime start, LocalDateTime end) {
        return repository.findAllByTransactionDateBetween(start, end);
    }

    @Override
    public List<ParsedMessage> getAllByHex(String hex) {
        return repository.findAllByHex(hex);
    }

    @Override
    public List<ParsedMessage> getAllEdited(boolean edited) {
        return repository.findAllEdited(edited);
    }

    @Override
    public void deleteAllByMti(String mti) {
        repository.deleteAllByMti(mti);
    }

    @Override
    public void deleteByTransactionNumber(String transactionNumber) {
        repository.deleteByTransactionNumber(transactionNumber);
    }

    @Override
    public void deleteAllByTransactionDateBetween(LocalDateTime start, LocalDateTime end) {
        repository.deleteAllByTransactionDateBetween(start, end);
    }

    @Override
    public void deleteAllByHex(String hex) {
        repository.deleteAllByHex(hex);
    }

    @Override
    public void deleteAllByEdited(boolean edited) {
        repository.deleteAllByEdited(edited);
    }
}

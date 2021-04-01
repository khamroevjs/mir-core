package mir.routing.emulator;

import com.imohsenb.ISO8583.exceptions.ISOException;
import mir.change.Changer;
import mir.models.ParsedMessage;
import mir.parsing.routing.Router;
import mir.services.IMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;

@RestController
@RequestMapping("/main")
public class Issuer {

    private final IMessageService service;

    @Autowired
    public Issuer(IMessageService service) {
        this.service = service;
    }

    @GetMapping(path = "/api")
    public ResponseEntity<String> getRequest(@RequestParam(name = "Payload") String payload) {
        if (payload != null && !payload.isBlank()) {
            try {
                ParsedMessage parsedMessage = Router.getParsedMessage(payload);
                service.add(parsedMessage);

                ParsedMessage formedMessage = Changer.formResponse(parsedMessage);
                service.add(formedMessage);

                String respText = Router.getEncodedMessage(formedMessage);

                // Return response.
                return new ResponseEntity<>(respText, HttpStatus.OK);
            } catch (IOException neverThrown) {
                return new ResponseEntity<>(neverThrown.getMessage(), HttpStatus.BAD_REQUEST);
            } catch (ISOException ex) {
                return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>("Query can't be empty", HttpStatus.BAD_REQUEST);
        }
    }
}

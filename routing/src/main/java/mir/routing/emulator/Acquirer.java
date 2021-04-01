package mir.routing.emulator;

import com.imohsenb.ISO8583.exceptions.ISOException;

import java.io.IOException;
import java.util.List;

import mir.change.Changer;
import mir.check.Checker;
import mir.models.MessageError;
import mir.models.ParsedMessage;
import mir.parsing.routing.Router;
import mir.services.IMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/main")
public class Acquirer {

    private final IMessageService service;

    @Autowired
    public Acquirer(IMessageService service) {
        this.service = service;
    }

    private final String URI = "https://mir-platform.herokuapp.com/main/api";

    private String sendRequest(String hex) {
        // Form new Http-request to Platform and get response from it.
        RestTemplate restTemplate = new RestTemplate();

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(URI)
                .queryParam("Payload", hex);

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                uriBuilder.toUriString(),
                HttpMethod.GET,
                null,
                String.class
        );

        return responseEntity.getBody();
    }

    @GetMapping(path = "/api")
    public ResponseEntity<String> getRequest(@RequestParam(name = "Payload") String payload) {
        if (payload != null && !payload.isBlank()) {
            try {
                // Check.
                ParsedMessage parsedMessage = Router.getParsedMessage(payload);
                List<MessageError> errorsList = Checker.checkParsedMessage(parsedMessage);

                String respText;
                if (errorsList.size() == 0) {
                    // --- CORRECT PARAM CONTENT --- //
                    ParsedMessage formedMessage = Changer.completeParsedMessageRequest(parsedMessage);

                    service.add(formedMessage);

                    // Send request to platform and get response.
                    respText = sendRequest(Router.getEncodedMessage(formedMessage));

                    // Return response from Issuer.
                    return new ResponseEntity<>(respText, HttpStatus.OK);
                } else {
                    // --- INCORRECT PARAM CONTENT --- //
                    StringBuilder errors = new StringBuilder();

                    for (var error : errorsList) {
                        errors.append(error.getMessage()).append("\n");
                    }
                    respText = String.format("Incorrect payload content format.\n%s", errors.toString());

                    // Return error response immediately.
                    return new ResponseEntity<>(respText, HttpStatus.BAD_REQUEST);
                }
            } catch (IOException neverThrowed) {
                return new ResponseEntity<>(neverThrowed.getMessage(), HttpStatus.BAD_REQUEST);
            } catch (ISOException ex) {
                return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
            } catch (NoSuchFieldException ex) {
                return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
            } catch (IllegalAccessException neverThrown) {
                return new ResponseEntity<>(neverThrown.getMessage(), HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>("Message is empty", HttpStatus.BAD_REQUEST);
        }
    }
}

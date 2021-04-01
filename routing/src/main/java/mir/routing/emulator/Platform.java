package mir.routing.emulator;

import com.imohsenb.ISO8583.exceptions.ISOException;
import mir.models.ParsedMessage;
import mir.parsing.routing.Router;
import mir.services.IMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.*;

@RestController
@RequestMapping("/main")
public class Platform {

    private final IMessageService service;

    @Autowired
    public Platform(IMessageService service) {
        this.service = service;
    }

    private final String URI = "https://mir-issuer.herokuapp.com/main/api";
    private final String LINK_URI = "https://yar.cx/links/transaction";

    private String sendRequest(String hex) {
        // Form new Http-request to Issuer and get response from it.
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

    private String sendRequestToLink(String hex){
        RestTemplate restTemplate = new RestTemplate();

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(LINK_URI)
                .queryParam("Payload", hex);

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                uriBuilder.toUriString(),
                HttpMethod.POST,
                null,
                String.class);

        return responseEntity.getBody();
    }

    @GetMapping(path = "/api")
    public ResponseEntity<String> getRequest(@RequestParam(name = "Payload") String payload) {
        if (payload != null && !payload.isBlank()) {
            try {
                // Check.
                ParsedMessage parsedMessage = Router.getParsedMessage(payload);

                service.add(parsedMessage);

                // Send request to platform and get response.
                String respText = sendRequest(Router.getEncodedMessage(parsedMessage));

                // Return response from Platform.
                var response = Router.getParsedMessage(respText);
                service.add(response);
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

    @GetMapping(path = "/link-api")
    public ResponseEntity<String> getLinkRequest(@RequestParam(name = "Payload") String payload) {
        if (payload != null && !payload.isBlank()) {
            try {
                // Check.
                ParsedMessage parsedMessage = Router.getParsedMessage(payload);

                service.add(parsedMessage);

                String respText = sendRequestToLink(Router.getEncodedMessage(parsedMessage));
                var response = Router.getParsedMessage(respText);
                service.add(response);

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

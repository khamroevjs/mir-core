package mir.routing.emulator;

import com.imohsenb.ISO8583.exceptions.ISOException;

import java.io.IOException;
import java.util.List;

import io.swagger.annotations.Api;
import mir.change.Changer;
import mir.check.Checker;
import mir.models.MessageError;
import mir.models.ParsedMessage;
import mir.parsing.routing.Router;
import mir.services.CardService;
import mir.services.IMessageService;
import mir.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/main")
public class Acquirer {

    private final IMessageService messageService;
    private final UserService userService;
    private final CardService cardService;

    @Autowired
    public Acquirer(IMessageService messageService, UserService userService, CardService cardService) {
        this.messageService = messageService;
        this.userService = userService;
        this.cardService = cardService;
    }

    private final String URI = "https://mir-platform.herokuapp.com/main/api";
    private final String URI_FOR_LINK_REQUEST = "https://mir-platform.herokuapp.com/main/link-api";

    private String sendRequest(String hex, String uri) {
        // Form new Http-request to Platform and get response from it.
        RestTemplate restTemplate = new RestTemplate();

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(uri)
                .queryParam("Payload", hex);

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                uriBuilder.toUriString(),
                HttpMethod.GET,
                null,
                String.class
        );

        return responseEntity.getBody();
    }

    @GetMapping(path = "/fit-api")
    public ResponseEntity<String> getFitRequest(@RequestParam(name = "Payload") String payload) {
        if (payload != null && !payload.isBlank()) {
            try {
                // Check.
                ParsedMessage parsedMessage = Router.getParsedMessage(payload);
                List<MessageError> errorsList = Checker.checkParsedMessage(parsedMessage);

                String respText;
                if (errorsList.size() == 0) {
                    // --- CORRECT PARAM CONTENT --- //
                    ParsedMessage formedMessage = Changer.completeParsedMessageRequest(parsedMessage);

                    // Retrieving card number
                    var cardNumberParsedField = parsedMessage.getFields().get(2);
                    String cardNumber;
                    if (cardNumberParsedField == null)
                        return new ResponseEntity<>("Card number is not provided", HttpStatus.BAD_REQUEST);
                    else
                        cardNumber = cardNumberParsedField.getContent();

                    if (!userService.existsByCardNumber(cardNumber))
                        return new ResponseEntity<>("User with given number doesn't exists", HttpStatus.BAD_REQUEST);

                    var typeOfOperation = parsedMessage.getFields().get(3).getSubfields().get(1).getContent();
                    String moneyString = parsedMessage.getFields().get(4).getContent();
                    Long money = Long.getLong(moneyString);


                    if (typeOfOperation.compareTo("00") == 0) {
                        try{
                            userService.writeOffMoney(cardNumber, money);
                        }
                        catch (IllegalStateException exception){
                            return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
                        }
                    } else if (typeOfOperation.compareTo("28") == 0) {
                        userService.depositMoney(cardNumber, money);
                    } else
                        return new ResponseEntity<>("Operation is not recognized", HttpStatus.BAD_REQUEST);

                    messageService.add(formedMessage);

                    // Send request to platform and get response.
                    respText = sendRequest(Router.getEncodedMessage(formedMessage), URI);
                    var response = Router.getParsedMessage(respText);
                    messageService.add(response);
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

    @GetMapping(path = "/fundraising-api")
    public ResponseEntity<String> getFundraisingRequest(@RequestParam(name = "Payload") String payload) {
        if (payload != null && !payload.isBlank()) {
            try {
                // Check.
                ParsedMessage parsedMessage = Router.getParsedMessage(payload);
                List<MessageError> errorsList = Checker.checkParsedMessage(parsedMessage);

                String respText;
                if (errorsList.size() == 0) {
                    // --- CORRECT PARAM CONTENT --- //
                    ParsedMessage formedMessage = Changer.completeParsedMessageRequest(parsedMessage);

                    // Retrieving card number
                    var cardNumberParsedField = parsedMessage.getFields().get(2);
                    String cardNumber;
                    if (cardNumberParsedField == null)
                        return new ResponseEntity<>("Card number is not provided", HttpStatus.BAD_REQUEST);
                    else
                        cardNumber = cardNumberParsedField.getContent();

                    if (!cardService.existsByNumber(cardNumber))
                        return new ResponseEntity<>("Card with given number doesn't exists", HttpStatus.BAD_REQUEST);

                    String typeOfOperation = parsedMessage.getFields().get(3).getSubfields().get(1).getContent();
                    String moneyString = parsedMessage.getFields().get(4).getContent();
                    Long money = Long.getLong(moneyString);

                    if (typeOfOperation.compareTo("00") == 0) {
                        try {
                            cardService.writeOffMoney(cardNumber, money);
                        } catch (IllegalStateException exception) {
                            return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
                        }
                    } else if (typeOfOperation.compareTo("28") == 0)
                        cardService.depositMoney(cardNumber, money);
                    else
                        return new ResponseEntity<>("Transaction operation is not recognized", HttpStatus.BAD_REQUEST);

                    messageService.add(formedMessage);

                    // Send request to platform and get response.
                    respText = sendRequest(Router.getEncodedMessage(formedMessage), URI);
                    var response = Router.getParsedMessage(respText);
                    messageService.add(response);
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

    @GetMapping(path = "/link-api")
    public ResponseEntity<String> getLinkRequest(@RequestParam(name = "Payload") String payload) {
        if (payload != null && !payload.isBlank()) {
            try {
                // Check.
                ParsedMessage parsedMessage = Router.getParsedMessage(payload);
                List<MessageError> errorsList = Checker.checkParsedMessage(parsedMessage);

                String respText;
                if (errorsList.size() == 0) {
                    // --- CORRECT PARAM CONTENT --- //

                    ParsedMessage formedMessage = Changer.completeParsedMessageRequest(parsedMessage);
                    messageService.add(formedMessage);

                    // Send request to platform and get response.
                    respText = sendRequest(Router.getEncodedMessage(formedMessage), URI_FOR_LINK_REQUEST);

                    // Return response from Issuer.
                    var response = Router.getParsedMessage(respText);
                    messageService.add(response);
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

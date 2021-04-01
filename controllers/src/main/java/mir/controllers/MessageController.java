package mir.controllers;

import mir.models.Card;
import mir.models.ParsedMessage;
import mir.services.CardService;
import mir.services.IMessageService;
import mir.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/info")
public class MessageController {

    private final IMessageService messageService;
    private final CardService cardService;
    private final UserService userService;

    @Autowired
    public MessageController(IMessageService service, CardService cardService, UserService userService) {
        this.messageService = service;
        this.cardService = cardService;
        this.userService = userService;
    }

    /**
     * For fundraising. Registers card
     * @param card card
     * @return card
     */
    @PostMapping("/register-card")
    public Card registerCard(@RequestBody Card card){
        return cardService.registerCard(card);
    }

    /**
     * For Fit
     * @param cardNumber card number
     * @return true - if exists, false - otherwise
     */
    @GetMapping( "/user-exists")
    public boolean userExists(@RequestParam String cardNumber){
        return userService.existsByCardNumber(cardNumber);
    }

    /**
     * For Fit
     * Example: 2021-03-07T21:35:44
     *
     * @param start start date
     * @param end   end date
     * @return list of messages
     */
    @GetMapping(path = "/get-transactions-by-date-range")
    public List<ParsedMessage> getAllByTransactionDateBetween(
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @RequestParam LocalDateTime start,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @RequestParam LocalDateTime end) {

        return messageService.getAllByTransactionDateBetween(start, end);
    }

    @GetMapping(path = "/all-messages")
    public List<ParsedMessage> getMessages() {
        return messageService.getAll();
    }

//    Examples:
//
//    @ApiResponses(value = {
//            @ApiResponse(code = 200, message = "Client successfully saved"),
//            @ApiResponse(code = 400, message = "The user already exists")
//    })
//
//    @PostMapping
//    public void addMessage(@RequestBody ParsedMessage parsedMessage) {
//        service.addMessage(parsedMessage);
//    }
//
//    @DeleteMapping(path = "{id}")
//    public void deleteMessage(@PathVariable("id") Integer id) {
//        service.deleteMessageById(id);
//    }
//
//    @PutMapping(path = "{id}")
//    public void updateMessage(@PathVariable("id") Integer id,
//                              @RequestBody ParsedMessage parsedMessage) {
//        service.updateMessage(id, parsedMessage);
//    }
}

package mir.controllers;

import mir.models.ParsedMessage;
import mir.services.IMessageService;
import mir.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/info")
public class MessageController {

    private final IMessageService messageService;
    private final UserService userService;

    @Autowired
    public MessageController(IMessageService service, UserService userService) {
        this.messageService = service;
        this.userService = userService;
    }

    /**
     * For Fit
     * @param cardNumber card number
     * @return true - if exists, false - otherwise
     */
    @GetMapping(path = "/card-number-exists")
    public boolean cardNumberExists(String cardNumber){
        return userService.exists(cardNumber);
    }

    /**
     * For Fit
     * Example: 2021-03-07T21:35:44
     *
     * @param start start date
     * @param end   end date
     * @return list of messages
     */
    @GetMapping(path = "/get-by-date-range")
    public List<ParsedMessage> getAllByTransactionDateBetween(
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @RequestParam LocalDateTime start,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @RequestParam LocalDateTime end) {

        return messageService.getAllByTransactionDateBetween(start, end);
    }

    @GetMapping(path = "/welcome")
    public String welcome() {
        return "Welcome to Mir-Core";
    }

    @GetMapping(path = "/all-messages")
    public List<ParsedMessage> getMessages() {
        return messageService.getAll();
    }


    //    @ApiResponses(value = {
//            @ApiResponse(code = 200, message = "Client successfully saved"),
//            @ApiResponse(code = 400, message = "The user already exists")
//    })
//    // TODO: 3/6/2021 Will be removed
//    @Deprecated
//    @PostMapping
//    public void addMessage(@RequestBody ParsedMessage parsedMessage) {
//        service.addMessage(parsedMessage);
//    }
//
//    // TODO: 3/6/2021 Will be removed
//    @Deprecated
//    @DeleteMapping(path = "{id}")
//    public void deleteMessage(@PathVariable("id") Integer id) {
//        service.deleteMessageById(id);
//    }
//
//    // TODO: 3/6/2021 Will be removed
//    @Deprecated
//    @PutMapping(path = "{id}")
//    public void updateMessage(@PathVariable("id") Integer id,
//                              @RequestBody ParsedMessage parsedMessage) {
//        service.updateMessage(id, parsedMessage);
//    }
}

package mir.controllers;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import mir.models.ParsedMessage;
import mir.services.IMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping
public class MessageController {

    private final IMessageService service;

    @Autowired
    public MessageController(IMessageService service) {
        this.service = service;
    }

    //    @ApiResponses(value = {
//            @ApiResponse(code = 200, message = "Client successfully saved"),
//            @ApiResponse(code = 400, message = "The user already exists")
//    })
    @GetMapping(path = "/welcome")
    public String welcome() {
        return "Welcome to Mir-Core";
    }

    @GetMapping(path = "/messages")
    public List<ParsedMessage> getMessages() {
        return service.getAll();
    }

    /**
     * Example: 2021-03-07T21:35:44
     * @param start start date
     * @param end end date
     * @return list of messages
     */
    @GetMapping(path = "/date")
    public List<ParsedMessage> getAllByTransactionDateBetween(
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @RequestParam LocalDateTime start,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @RequestParam LocalDateTime end) {

        return service.getAllByTransactionDateBetween(start, end);
    }

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

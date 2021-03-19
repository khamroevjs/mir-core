package mir.controllers;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import mir.models.ParsedMessage;
import mir.services.IMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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

    /*
    @GetMapping("/welcome")
    public String welcome(){
        return "Welcome to Acquirer";
    }

     */

    @PostMapping("/send-request")
    public String sendRequest(@RequestParam String message){
        service.saveMessage(message);
        return "Success";
    }

    /*
    @GetMapping(path = "/messages")
    public List<ParsedMessage> getMessages() {
        return service.getAll();
    }
     */

    /**
     * Example: 2021-03-07T21:35:44
     * @param start start date
     * @param end end date
     * @return list of messages
     */
    /*
    @GetMapping(path = "/date")
    public List<ParsedMessage> getAllByTransactionDateBetween(
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @RequestParam LocalDateTime start,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @RequestParam LocalDateTime end) {

        return service.getAllByTransactionDateBetween(start, end);
    }

     */


    //region Examples
    /*
    @PostMapping
    public void addMessage(@RequestBody ParsedMessage parsedMessage) {
        service.add(parsedMessage);
    }

    @DeleteMapping(path = "{id}")
    public void deleteMessage(@PathVariable("id") Integer id) {
        service.deleteById(id);
    }

    @PutMapping(path = "{id}")
    public void updateMessage(@PathVariable("id") Integer id,
                              @RequestBody ParsedMessage parsedMessage) {
        service.update(id, parsedMessage);
    }

    //    @ApiResponses(value = {
//            @ApiResponse(code = 200, message = "Client successfully saved"),
//            @ApiResponse(code = 400, message = "The user already exists")
//    })
     */
    //endregion
}

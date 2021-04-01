package mir.controllers;

import mir.models.ParsedMessage;
import mir.services.IMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/info")
public class MessageController {

    private final IMessageService service;

    @Autowired
    public MessageController(IMessageService service) {
        this.service = service;
    }

    @GetMapping(path = "/all-messages")
    public List<ParsedMessage> getMessages() {
        return service.getAll();
    }

/*  Examples:

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Client successfully saved"),
            @ApiResponse(code = 400, message = "The user already exists")
    })
    @DeleteMapping(path = "{id}")
    public void deleteMessage(@PathVariable("id") Integer id) {
        service.deleteMessageById(id);
    }

    @PutMapping(path = "{id}")
    public void updateMessage(@PathVariable("id") Integer id,
                              @RequestBody ParsedMessage parsedMessage) {
        service.updateMessage(id, parsedMessage);
    }
 */
}

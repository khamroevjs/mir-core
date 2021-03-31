package mir.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import mir.annotation.ControllerTestConfiguration;
import mir.models.ParsedMessage;
import mir.services.IMessageService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
@ControllerTestConfiguration(controllers = MessageController.class)
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MessageController messageController;

    @MockBean
    private IMessageService iMessageService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void givenNone_whenGetMessages_thenReturnAllMessages() throws Exception {
        ParsedMessage parsedMessage1 = new ParsedMessage();
        parsedMessage1.setId(1);
        ParsedMessage parsedMessage2 = new ParsedMessage();
        parsedMessage1.setId(2);
        ParsedMessage parsedMessage3 = new ParsedMessage();
        parsedMessage1.setId(3);

        List<ParsedMessage> expected = List.of(
                parsedMessage1,
                parsedMessage2,
                parsedMessage3
        );
        doReturn(expected).when(iMessageService).getAll();

        final String strResponseBody = this.mockMvc.perform(get("/messages")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        final List<ParsedMessage> actual = this.objectMapper.readValue(strResponseBody, new TypeReference<>() {
        });

//        тут надо как-то сравнить эти 2 листа
//        Assertions.assertEquals(expected, actual);

        verify(this.iMessageService, times(1))
                .getAll();
    }

}

 */

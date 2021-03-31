import com.imohsenb.ISO8583.exceptions.ISOException;
import mir.change.Changer;
import mir.models.ParsedField;
import mir.models.ParsedMessage;
import mir.parsing.routing.Router;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class ChangerTest {

    /*
    The mti must became equal to 0110.
    Must be deleted the 12th, 13th, 23th fields.
    The edited must become false.
    (The message includes the 12th, 13th, 23th)
     */
    @Test
    void formResponseFor_0100_With_12_13_23() throws ISOException {
        // Fields: 3, 11, 12, 13, 23;
        // 3 - 920000
        // 11 - 000001
        // 12 - 023120
        // 13 - 0332
        // 23 - 0099
        String message = "0100203802000000000092000000000102312003320099";
        // Getting of the parsedMessage for the test.
        ParsedMessage request = Router.getParsedMessage(message);
        System.out.println("REQUEST PARSED_MESSAGE");
        System.out.println(request);
        ParsedMessage response = Changer.formResponse(request);
        System.out.println("RESPONSE PARSED_MESSAGE");
        System.out.println(response);
        // Check of the result.
        assertEquals("0110", response.getMti());
        assertEquals(true, response.getEdited());
        assertEquals(0, message.compareTo(response.getHex()));
        HashMap<Integer, ParsedField> fields = response.getFields();
        assertEquals(true, fields.containsKey(3));
        assertEquals(true, fields.containsKey(11));
        assertEquals(false, fields.containsKey(12));
        assertEquals(false, fields.containsKey(13));
        assertEquals(false, fields.containsKey(23));
    }

    /*
    The mti must became equal to 0410.
    Must be deleted the 12th, 13th, 23th fields.
    The edited must become false.
     */
    @Test
    void formResponseFor_0400() throws ISOException {
        // Fields: 3, 11, 12, 13, 23;
        // 3 - 920000
        // 11 - 000001
        // 12 - 023120
        // 13 - 0332
        // 23 - 0099
        String message = "0400203802000000000092000000000102312003320099";
        // Getting of the parsedMessage for the test.
        ParsedMessage request = Router.getParsedMessage(message);
        System.out.println("REQUEST PARSED_MESSAGE");
        System.out.println(request);
        ParsedMessage response = Changer.formResponse(request);
        System.out.println("RESPONSE PARSED_MESSAGE");
        System.out.println(response);
        // Check of the result.
        assertEquals("0410", response.getMti());
        assertEquals(true, response.getEdited());
        assertEquals(0, message.compareTo(response.getHex()));
        HashMap<Integer, ParsedField> fields = response.getFields();
        assertEquals(true, fields.containsKey(3));
        assertEquals(true, fields.containsKey(11));
        assertEquals(false, fields.containsKey(12));
        assertEquals(false, fields.containsKey(13));
        assertEquals(false, fields.containsKey(23));

    }

    /*
    The mti must became equal to 0110.
    Must be deleted the 12th, 13th, 23th fields.
    The edited must become false.
    (The message does not include the 12th, 13th, 23th.
    But a message with the mti 0100 which is avoiding into the Change Module
    always has these fields)
     */
    @Test
    void formResponseFor_0100_Without_12_13_23() throws ISOException {
        // Fields: 3, 11;
        // 3 - 920000
        // 11 - 000001
        String message = "01002020000000000000920000000001";
        // Getting of the parsedMessage for the test.
        ParsedMessage request = Router.getParsedMessage(message);
        System.out.println("REQUEST PARSED_MESSAGE");
        System.out.println(request);
        ParsedMessage response = Changer.formResponse(request);
        System.out.println("RESPONSE PARSED_MESSAGE");
        System.out.println(response);
        // Check of the result.
        assertEquals("0110", response.getMti());
        assertEquals(true, response.getEdited());
        assertEquals(0, message.compareTo(response.getHex()));
        HashMap<Integer, ParsedField> fields = response.getFields();
        assertEquals(true, fields.containsKey(3));
        assertEquals(true, fields.containsKey(11));
        assertEquals(false, fields.containsKey(12));
        assertEquals(false, fields.containsKey(13));
        assertEquals(false, fields.containsKey(23));
    }

    /*
    Must fail because the message with this mti is not expected by the mir.change.Changer.
     */
    @Test
    void formResponseFor_0800() throws ISOException {
        // Fields: 3, 11;
        // 3 - 920000
        // 11 - 000001
        String message = "08002020000000000000920000000001";
        // Getting of the parsedMessage for the test.
        ParsedMessage request = Router.getParsedMessage(message);
        System.out.println("REQUEST PARSED_MESSAGE");
        System.out.println(request);
        try {
            ParsedMessage response = Changer.formResponse(request);
        }
        catch (IllegalArgumentException ex) {
            assertEquals("0100 or 0400 message were expected!", ex.getMessage());
        }
    }
}
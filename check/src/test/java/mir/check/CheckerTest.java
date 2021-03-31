package mir.check;

import com.imohsenb.ISO8583.builders.ISOMessageBuilder;
import com.imohsenb.ISO8583.entities.ISOMessage;
import com.imohsenb.ISO8583.enums.*;
import com.imohsenb.ISO8583.exceptions.ISOException;
import com.imohsenb.ISO8583.utils.StringUtil;
import mir.models.*;
import mir.parsing.routing.Router;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.fail;

class CheckerTest {

    /*
    Tests the correctness of the mti.
    The mti value must belong to the allowable set of values.
    There this condition is provided.
    The test must fail if the mistake about the mti is included in errors list.
     */
    @Test
    void testParsedMessageMtiCorrect() throws IOException, ISOException, NoSuchFieldException, IllegalAccessException {
        // 3, 48.
        // String probaMessage = "0100200000000001000092000000092536313034696E"; // +

        // Test of the table.
        // 22 (n, 3)
        // String probaMessage = "010000000400000000008108"; // +
        // 32 (n, 1-11, LL) - Here the length of a number is twice less than its length according to the MIP (1 num = 1/2 byte).
        // String probaMessage = "01000000000100000000030123"; // +
        // 52 (b, 8)
        // String probaMessage = "0100000000000000100088888888"; // +
        // 39 (an, 2) - Here the length of a number is equal to its length according to the MIP (1 num = 1 byte).
        // String probaMessage = "010000000000020000003339"; // +
        // 41 (ans, 8)
        // String probaMessage = "010000000000008000003031323334353637"; // +
        // 35 (ans, 1-37, LL)
         String probaMessage = "01000000000020000000023337"; // +
        // 48 (ans, 6-999, LLL)
        // The 97th subfield of the 48th field.
        // String probaMessage = "01000000000000010000000925363130390999999999"; // len of the 48th field = 0009. // +
        // The 63th field. The 2th subfield.
        // String probaMessage = "010000000000000000023030303023232323232323234D4D4D"; // +
        ISOMessage isoMessageProba = null;
        try {
            isoMessageProba = ISOMessageBuilder.Unpacker()
                    .setMessage(probaMessage)
                    .build();
        }
        catch (StringIndexOutOfBoundsException ex) {
            System.out.println(ex.getMessage());
            return;
        }
        catch (ISOException ex) {
            System.out.println(ex.getMessage());
            return;
        }
        System.out.println(isoMessageProba.toString());
        /*int fieldId = 48;
        String fieldStr = null;
        if (FIELDS.valueOf(fieldId).getType().compareTo("n") == 0 ||
                FIELDS.valueOf(fieldId).getType().compareTo("b") == 0) {
            fieldStr = isoMessageProba.getStringField(fieldId);
            System.out.println(Integer.parseInt(fieldStr));
        }
        else
            fieldStr = StringUtil.hexToAscii(isoMessageProba.getStringField(fieldId));
        System.out.println(fieldStr);*/

        ParsedMessage parsedMessage = Router.getParsedMessage(probaMessage);
        parsedMessage.setTransactionDate(LocalDateTime.now());
        System.out.println(parsedMessage);

        Checker checker = new Checker();
        List<MessageError> errors = checker.checkParsedMessage(parsedMessage);
        System.out.println("Errors:");
        for (MessageError error : errors) {
            System.out.println("error: " + error.getMessage());
            if (error.getMessage().compareTo("The value of the mti of the ParsedMessage" +
                    " is not one from the allowable set!") == 0)
                fail("The error of the incorrect mti.");
        }
    }
}
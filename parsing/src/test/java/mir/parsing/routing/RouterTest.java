package mir.parsing.routing;

import com.imohsenb.ISO8583.builders.ISOMessageBuilder;
import com.imohsenb.ISO8583.entities.ISOMessage;
import com.imohsenb.ISO8583.enums.FIELDS;
import com.imohsenb.ISO8583.enums.MESSAGE_FUNCTION;
import com.imohsenb.ISO8583.enums.MESSAGE_ORIGIN;
import com.imohsenb.ISO8583.enums.VERSION;
import com.imohsenb.ISO8583.exceptions.ISOException;
import com.imohsenb.ISO8583.utils.StringUtil;
import mir.models.ParsedElement;
import mir.models.ParsedField;
import mir.models.ParsedMessage;
import mir.models.ParsedSubfield;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class RouterTest {

    @Test
    void getParsedMessage() throws IOException, ISOException {
        // Fields: 3, 11, 12, 13;
        String encodedMessage = "080020380000000000009200000000010231200332";
        ParsedMessage parsedMessage = Router.getParsedMessage(encodedMessage);
        System.out.println(parsedMessage);
    }

    @Test
    void getParseMessageWith_F37() throws ISOException {
        // Fields: 7, 11, 37.
        String encodedMessage = "010002200000080000020330163303000001108916000001";
        ParsedMessage parsedMessage = Router.getParsedMessage(encodedMessage);
        System.out.println(parsedMessage);
    }

    /*
    A getting hex is equal to the hex of the parsedMessage.
    */
    @Test
    void getEncodedMessageOfUneditedParsedMessage() throws IOException, ISOException {
        // Fields: 3, 11, 12, 13;
        String initialEncodedMessage = "080020380000000000009200000000010231200332";
        ParsedMessage parsedMessage = Router.getParsedMessage(initialEncodedMessage);
        System.out.println(parsedMessage);

        String encodedMessageBack = Router.getEncodedMessage(parsedMessage);
        System.out.println("initial encodedMessage = " + initialEncodedMessage);
        System.out.println("encodedMessage\t\t   = " + encodedMessageBack);
    }

    /*
    Getting hex if forming by the Parsing Module
    and is different form the hex of the parsedMessage.
     */
    @Test
    void getEncodedMessageOfEditedParsedMessage() throws ISOException, IOException {
        // Fields: 3, 11, 12, 13;
        String initialEncodedMessage = "080020380000000000009200000000010231200332";
        ParsedMessage parsedMessage = Router.getParsedMessage(initialEncodedMessage);
        System.out.println("OLD PARSED_MESSAGE");
        System.out.println(parsedMessage);

        // New field 6. Inserted into the middle of the others.
        /*ParsedField newParsedField = new ParsedField();
        newParsedField.setId(6);
        newParsedField.setType("n");
        newParsedField.setLengthMIP(12);
        newParsedField.setContent("121212121212");*/ // 121212121212 in hex.

        // New field 32 (n, 1-11, LL). Has an unfixed length LL.
        // - Here the length of a number is twice less than its length according to the MIP (1 num = 1/2 byte).
        /*ParsedField newParsedField = new ParsedField();
        newParsedField.setId(32);
        newParsedField.setType("n");
        newParsedField.setLengthMIP(3);
        newParsedField.setContent("0123");*/ // 0123 in hex.

        // New field 48 (ans, 6-999, LLL). Has an unfixed length LLL.
        /*ParsedField newParsedField = new ParsedField();
        newParsedField.setId(48);
        newParsedField.setType("ans");
        newParsedField.setLengthMIP(9);
        newParsedField.setContent("25363130390999999999");
        // The 97th subfield of the 48th field.
        ParsedElement newParsedElement = new ParsedElement();
        newParsedElement.setId(97);
        newParsedElement.setType("b");
        newParsedElement.setLengthMIP(9);
        newParsedElement.setContent("0999999999"); // 0999999999 in hex.
        // Add to the parsedField.
        newParsedField.addElement(newParsedElement);
        newParsedField.setHasElements(true);*/

        // New field 39 (an, 2)
        // - Here the length of a number is equal to its length according to the MIP (1 num = 1 byte).
        /*ParsedField newParsedField = new ParsedField();
        newParsedField.setId(39);
        newParsedField.setType("an");
        newParsedField.setLengthMIP(2);
        newParsedField.setContent("39"); // 3339 in hex*/

        // New field 52 (b, 8)
        /*ParsedField newParsedField = new ParsedField();
        newParsedField.setId(52);
        newParsedField.setType("b");
        newParsedField.setLengthMIP(8);
        newParsedField.setContent("88888888"); // 88888888 in hex.*/

        // New field 63 (ans, 23).
        //String probaMessage = "010000000000000000023030303023232323232323234D4D4D"; // +
        ParsedField newParsedField = new ParsedField();
        newParsedField.setId(63);
        newParsedField.setType("ans");
        newParsedField.setLengthMIP(23);
        newParsedField.setContent("3030303023232323232323234D4D4D"); // 88888888 in hex.
        // The 1th subfield.
        ParsedSubfield parsedSubfield_1 = new ParsedSubfield();
        parsedSubfield_1.setId(1);
        parsedSubfield_1.setType("an");
        parsedSubfield_1.setLengthMIP(4);
        parsedSubfield_1.setContent("0000"); // 30303030 in hex.
        // The 2th subfield.
        ParsedSubfield parsedSubfield_2 = new ParsedSubfield();
        parsedSubfield_2.setId(2);
        parsedSubfield_2.setType("n");
        parsedSubfield_2.setLengthMIP(16);
        parsedSubfield_2.setContent("2323232323232323"); // 2323232323232323 in hex.
        // The 3th subfield.
        ParsedSubfield parsedSubfield_3 = new ParsedSubfield();
        parsedSubfield_3.setId(3);
        parsedSubfield_3.setType("ans");
        parsedSubfield_3.setLengthMIP(3);
        parsedSubfield_3.setContent("MMM"); // 4D4D4D in hex.
        // Addition of the subfields to the newParsedField.
        newParsedField.setSubfield(parsedSubfield_1);
        newParsedField.setSubfield(parsedSubfield_2);
        newParsedField.setSubfield(parsedSubfield_3);
        newParsedField.setHasSubfields(true);

        parsedMessage.addField(newParsedField);
        parsedMessage.setEdited(true);

        System.out.println("NEW PARSED_MESSAGE");
        System.out.println(parsedMessage);
        System.out.println();

        // Comparison of the initial and getting encodedMessage.
        String encodedMessageBack = Router.getEncodedMessage(parsedMessage);
        System.out.println("initial encodedMessage = " + initialEncodedMessage);
        System.out.println("encodedMessage\t\t   = " + encodedMessageBack);
    }

    // Todo: the method can be used as en example of the work with the generation of ISOMessage.
    /*
	For debugging. Returns a generated ISOMessage.
	 */
//    static String generateISOMessage() throws ISOException {
//        // Message = "01000000000000010000000925363130390999999999";
//        // Fields: 11, 12, 13, 48 (the 97th subfield).
//        ISOMessage isoMessageService = new ISOMessage();
//        try {
//            isoMessageService = ISOMessageBuilder.Packer(VERSION.V1987)
//                    .networkManagement()
//                    .mti(MESSAGE_FUNCTION.Request, MESSAGE_ORIGIN.Acquirer)
//                    .processCode("920000")
//                    .setField(FIELDS.F11_STAN,  "1")
//                    .setField(FIELDS.F12_LocalTime,  "023120")
//                    .setField(FIELDS.F13_LocalDate,  "0332")
//                    .setField(FIELDS.F48_AddData_Private, "000925363130390999999999") // "0999999999"
//                    .setHeader("0000000000") // "0000000000"
//                    .build();
//        } catch (ISOException e) {
//            e.printStackTrace();
//        }
//        System.out.println("generated isoMessage = " + isoMessageService.toString());
//        System.out.println("primaryBitmap = " + StringUtil.fromByteArray(isoMessageService.getPrimaryBitmap()));
//        System.out.println("The 48th field = " + isoMessageService.getStringField(48));
//        return isoMessageService.toString();
//    }
}
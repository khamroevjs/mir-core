package mir.parsing.routing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imohsenb.ISO8583.builders.ISOMessageBuilder;
import com.imohsenb.ISO8583.entities.ISOMessage;
import com.imohsenb.ISO8583.enums.FIELDS;
import com.imohsenb.ISO8583.enums.MESSAGE_FUNCTION;
import com.imohsenb.ISO8583.enums.MESSAGE_ORIGIN;
import com.imohsenb.ISO8583.enums.VERSION;
import com.imohsenb.ISO8583.exceptions.ISOException;
import com.imohsenb.ISO8583.utils.StringUtil;
import mir.models.EncodedMessage;
import mir.models.ParsedMessage;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;

class RouterTest {

    @Test
    void getParsedMessage() throws IOException, ISOException {
        String encodedMessageJSON = generateEncodedMessageJSON();
        ParsedMessage parsedMessage = Router.getParsedMessage(encodedMessageJSON);
        System.out.println("getParsedMessage is ended");

        String encodedMessageJSOMBack = Router.getEncodedMessage(parsedMessage);
        System.out.println("getEncodedMessage is ended");
    }

    static void allProcess() throws ISOException, IOException {
        // ---------------------------------------------------------------------------------------
        // On the side of the services.

        // Creation an unparsed ISO-message.
        ISOMessage isoMessageService = ISOMessageBuilder.Unpacker()
                .setMessage(generateISOMessage())
                .build();
        // Creation of the JSON-wrapper for the ISO-message.
        EncodedMessage encodedServiceMessage = new EncodedMessage();
        // isoMessage without a header like it some service makes.
        encodedServiceMessage.message =
                isoMessageService.toString()
                        .substring(10 /*after the end of a header*/, isoMessageService.toString().length());
        System.out.println("encodedServiceMessage.message = " + encodedServiceMessage.message);
        System.out.println();


        String encodedMessageJSON = generateEncodedMessageJSON();
        // ----------------------------------------------------------------------------------------

        // ----------------------------------------------------------------------------------------
        // On the side of the Platform.

        // For parsing.
        // Like we has accepted this from the routing module.
        String encodedMessageJSONFromService = encodedMessageJSON;
        Router router = new Router();
        // This is that will be sent to routing module back.
        ParsedMessage parsedMessage = router.getParsedMessage(encodedMessageJSONFromService);

        StringWriter writer = new StringWriter();
        ObjectMapper mapper = new ObjectMapper();
        // The serialization for debugging.
        writer = new StringWriter();
        mapper.writeValue(writer, parsedMessage);

        // For encoding.
        String parsedMessageJSON2 = writer.toString();
        String encodedMessageJSON2 = router.getEncodedMessage(parsedMessage);
    }

    /*
    Generates an ISO-message without a header.
     */
    static String generateEncodedMessageJSON() throws ISOException, IOException {
        // Without a header.
        // Fields: 48
        // String myMessage = "0800000000000001000000165E303130333131315E30333033313135"
        // Fields: 37, 48
        // 37: "YJJJHHNNNNNN" - 136500
        // 37 requires 07 - "MMDDhhmmss" to get HH from hh
        // 37 requires 11 - to get NNNNNN from
        // Fields: /*11! (STAN)*/, 48
        // String myMessage = "0800000000000001000000165E303130333131315E30333033313135"; // LL before the 48th field.
        String myMessage = "080020380000000000009200000000010231200332"; // Fields: 3, 11!, 12, 13
        // String myMessage = "080020380000000010009200000000010231200332"
        System.out.println("myMessage = " + myMessage);
        ISOMessage myISOMessage = ISOMessageBuilder.Unpacker()
                .setMessage(myMessage)
                .build();
        System.out.println("1 = " + StringUtil.fromByteArray(myISOMessage.getBody()));


        String myMessage2 = "08002038000000010000920000000001023120033200165E303130333131315E30333033313135";
        ISOMessage myISOMessage2 = ISOMessageBuilder.Unpacker()
                .setMessage(myMessage2)
                .build();
        System.out.println("2 = " + StringUtil.fromByteArray(myISOMessage2.getBody()));


        /*System.out.println("F48. Hex = " + myISOMessage.getStringField(48, false));
        System.out.println("F48. ASCII = " + myISOMessage.getStringField(48, true));
        System.out.println("F48. Length = " + myISOMessage.getStringField(48, true).length());*/
        System.out.println("Hex = " + myISOMessage.getStringField(3, false));
        System.out.println("ASCII = " + myISOMessage.getStringField(3, true));
        System.out.println("Length = " + myISOMessage.getStringField(3, true).length());
        // System.out.println((int)'^');
        // System.out.println((int)'%');
        EncodedMessage myEncodedMessage = new EncodedMessage();
        myEncodedMessage.message = myMessage;

        StringWriter writer = new StringWriter();
        ObjectMapper mapper = new ObjectMapper();
        //mapper.writeValue(writer, encodedServiceMessage);
        mapper.writeValue(writer, myEncodedMessage);
        // It is that a service sends to the Platform.
        String encodedMessageJSON = writer.toString();
        return encodedMessageJSON;
    }

    /*
	For debugging. Returns a generated ISOMessage.
	 */
    static String generateISOMessage() throws ISOException {
        ISOMessage isoMessageService = new ISOMessage();
        try {
            isoMessageService = ISOMessageBuilder.Packer(VERSION.V1987)
                    .networkManagement()
                    .mti(MESSAGE_FUNCTION.Request, MESSAGE_ORIGIN.Acquirer)
                    .processCode("920000")
                    .setField(FIELDS.F11_STAN,  "1")
                    .setField(FIELDS.F12_LocalTime,  "023120")
                    .setField(FIELDS.F13_LocalDate,  "0332")
                    //.setField(FIELDS.F32_AcquiringInstitutionIdCode, "22222")
                    //.setField(FIELDS.F45_Track1, "7777777777777777777777777777777777777777777777777777777777777777777777777777")
                    .setField(FIELDS.F48_AddData_Private, "^0103222^97043937") // "^0103222"
                    //.setField(FIELDS.F57_Reserved_National, "%^222")
                    //.setField(FIELDS.F24_NII_FunctionCode,  "333")
                    .setHeader("0000000000") // "0000000000"
                    .build();
        } catch (ISOException e) {
            e.printStackTrace();
        }
        System.out.println("generated isoMessage = " + isoMessageService.toString());
        System.out.println("generated isoMessage. The initial form of the 48th field = "
                + isoMessageService.getStringField(48, true));
        System.out.println("primaryBitmap = " + StringUtil.fromByteArray(isoMessageService.getPrimaryBitmap()));
        System.out.println("length of the 48th field in the initial representation = " +
                isoMessageService.getField(48).length);
        System.out.println("The 48th field = " + isoMessageService.getStringField(48));
        return isoMessageService.toString();
    }
}
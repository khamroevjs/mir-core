package mir.change;

import com.imohsenb.ISO8583.exceptions.ISOException;
import mir.models.ParsedField;
import mir.models.ParsedMessage;
import mir.parsing.routing.Router;
import org.aspectj.weaver.Checker;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;

import static mir.change.Changer.*;
import static org.junit.jupiter.api.Assertions.*;

class ChangerTest {

    /*
    Adds the 7th, 11th, 37th fields to the parsedMessage
    which has not these fields.
    */
    @Test
    void testAddition_F7_F11_F37_ParsedMessageWithoutTheseFields() throws ISOException {
        // Fields: 3, 48.
        String message = "0100200000000001000092000000092536313034696E";

        ParsedMessage initialParsedMessage = Router.getParsedMessage(message);
        // Print.
        System.out.println("INITIAL PARSED_MESSAGE");
        System.out.println(initialParsedMessage);
        // Check of the result.
        HashMap<Integer, ParsedField> initialFields = initialParsedMessage.getFields();
        assertEquals(null, initialFields.get(7));
        assertEquals(null, initialFields.get(11));
        assertEquals(null, initialFields.get(37));
        assertEquals(null, initialParsedMessage.getTransactionDate());
        assertEquals(false, initialParsedMessage.getEdited());

        ParsedMessage changedParsedMessage = Changer.completeParsedMessageRequest(initialParsedMessage);
        // Print.
        System.out.println("CHANGED PARSED_MESSAGE");
        System.out.println(changedParsedMessage);
        // Check of the result.
        HashMap<Integer, ParsedField> changedFields = changedParsedMessage.getFields();
        assertNotEquals(null, changedFields.get(7));
        assertNotEquals(null, changedFields.get(11));
        assertNotEquals(null, changedFields.get(37));
        assertNotEquals(null, changedParsedMessage.getTransactionDate());
        assertEquals(true, changedParsedMessage.getEdited());
    }

    // Todo: complete or delete because the situation which is checked in this test
    //  is not provided by the requests of services.
    /*
    Adds the 7th, 11th, 37th fields to the parsedMessage
    which has these fields.
    (Good messages have not these fields but the Change Module can get these.
    In this situation, generating additional fields is replacing existed.)
    */
//    @Test
//    void testAddition_F7_F11_F37_ParsedMessageWithTheseFields() throws ISOException {
//        // Fields: 3, 7, 11, 37.
//        String message = "010022200000080000029200000330022013000001108902000001";
//        ParsedMessage initialParsedMessage = Router.getParsedMessage(message);
//        // Print.
//        System.out.println("INITIAL PARSED_MESSAGE");
//        System.out.println(initialParsedMessage);
//        ParsedMessage changedParsedMessage = Changer.completeParsedMessageRequest(initialParsedMessage);
//        System.out.println("CHANGED PARSED_MESSAGE");
//        System.out.println(changedParsedMessage);
//        // Check of the result.
//        HashMap<Integer, ParsedField> initialFields = initialParsedMessage.getFields();
//        assertEquals("0330022013", initialFields.get(7).getContent());
//        assertEquals("000001", initialFields.get(11).getContent());
//        assertEquals("108902000001", initialFields.get(37).getContent());
//        assertEquals(null, changedParsedMessage.getTransactionDate());
//        assertEquals(false, changedParsedMessage.getEdited());
//        /*get_F11(get_F11_content());
//        HashMap<Integer, ParsedField> changedFields = changedParsedMessage.getFields();
//        assertEquals(0, initialFields.get(7).getContent().compareTo(Checker.get));*/
//    }

    @Test
    void testGet_F7_content_WithAlignment() {
        LocalDateTime dateTime = LocalDateTime.of(2021, 1, 1, 1, 1, 1);
        String F7_content = get_F7_content(dateTime);
        // Format: "MMDDhhmmss".
        assertEquals("0101010101", F7_content);
    }

    @Test
    void testGet_F7_WithoutAlignment() {
        LocalDateTime dateTime = LocalDateTime.of(2021, 10, 10, 10, 10, 10);
        String F7_content = get_F7_content(dateTime);
        // Format: "MMDDhhmmss".
        assertEquals("1010101010", F7_content);
    }

    @Test
    void testGet_F7() {
        LocalDateTime dateTime = LocalDateTime.of(2021, 1, 1, 1, 1, 1);
        String F7_content = get_F7_content(dateTime);
        ParsedField F7 = get_F7(F7_content);
        assertEquals(7, F7.getId());
        assertEquals("n", F7.getType());
        assertEquals(10, F7.getLengthMIP());
        assertEquals("0101010101", F7.getContent());
    }

    @Test
    void testGet_F11_content_SameDay() {
        LocalDate date = LocalDate.of(2021, 10, 10);
        // Setting of the same date for the currentParsedMessageDate and for F11_content.
        Changer.currentParsedMessageDate = date;
        String F11_content = get_F11_content(date);
        assertEquals("000001", F11_content);
    }

    @Test
    void testGet_F11_content_NextDay() {
        LocalDate date_first = LocalDate.of(2021, 10, 10);
        // Setting of the same date for the currentParsedMessageDate and for F11_content_first.
        Changer.currentParsedMessageDate = date_first;
        String F11_content_first = get_F11_content(date_first);
        assertEquals("000001", F11_content_first);
        // Getting of the content of a ParsedMessage arrived next after previous day.
        LocalDate date_second = LocalDate.of(2021, 10, 11);
        // currentParsedMessageDate = date_first.
        String F11_content_second = get_F11_content(date_second);
        assertEquals("000001", F11_content_second);
    }

    @Test
    void testGet_F11_content_OverloadIndexLimit() {
        LocalDate date_first = LocalDate.of(2021, 10, 10);
        // Setting of the maximum value of the currentGlobalMIPTransactionNumber.
        Changer.currentGlobalMIPTransactionNumber = 1000000;
        // Setting of the same date for the currentParsedMessageDate and for F11_content_first.
        Changer.currentParsedMessageDate = date_first;
        String F11_content = get_F11_content(date_first);
        assertEquals("000001", F11_content);
    }

    @Test
    void testGet_F11() {
        LocalDate date = LocalDate.of(2021, 10, 10);
        // Setting of the same date for the currentParsedMessageDate and for F11_content.
        Changer.currentParsedMessageDate = date;
        String F11_content = get_F11_content(date);
        ParsedField F11 = get_F11(F11_content);

        assertEquals(11, F11.getId());
        assertEquals("n", F11.getType());
        assertEquals(6, F11.getLengthMIP());
        assertEquals("000001", F11.getContent());
    }

    @Test
    void testGet_F37_content_Alignment_2sym() {
        LocalDateTime dateTime = LocalDateTime.of(2021, 1, 1, 1, 1, 1);
        String F7_content = "0101010101";
        String F11_content = "999999";
        String F37_content = get_F37_content(dateTime, F7_content, F11_content);
        // Format: "YJJJHHNNNNNN".
        assertEquals("100101999999", F37_content);
    }

    @Test
    void testGet_F37_content_Alignment_1sym() {
        LocalDateTime dateTime = LocalDateTime.of(2021, 1, 10, 1, 1, 1);
        String F7_content = "0110010101";
        String F11_content = "999999";
        String F37_content = get_F37_content(dateTime, F7_content, F11_content);
        // Format: "YJJJHHNNNNNN".
        assertEquals("101001999999", F37_content);
    }

    @Test
    void testGet_F37_content_Alignment_0sym() {
        LocalDateTime dateTime = LocalDateTime.of(2021, 4, 30, 1, 1, 1);
        String F7_content = "0430010101";
        String F11_content = "999999";
        String F37_content = get_F37_content(dateTime, F7_content, F11_content);
        // Format: "YJJJHHNNNNNN".
        assertEquals("112001999999", F37_content);
    }


    @Test
    void testGet_F37() {
        LocalDateTime dateTime = LocalDateTime.of(2021, 4, 30, 1, 1, 1);
        String F7_content = "0430010101";
        String F11_content = "999999";
        // Format: "YJJJHHNNNNNN".
        String F37_content = get_F37_content(dateTime, F7_content, F11_content);
        ParsedField F37 = get_F37(F37_content);

        assertEquals(37, F37.getId());
        assertEquals("n", F37.getType());
        assertEquals(12, F37.getLengthMIP());
        assertEquals("112001999999", F37.getContent());
    }
}
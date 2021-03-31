package mir.parsing.formatters;

import mir.models.ParsedMessage;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

class EncoderTest {

    @Test
    void getHexFromParsedMessage() {
        ParsedMessage parsedMessage = new ParsedMessage();
        parsedMessage.setId(1);
        parsedMessage.setMti("0100");
        parsedMessage.setTransactionNumber("0000000000000001"); // The length == 16.
        parsedMessage.setTransactionDate(LocalDateTime.now());
        // Formation of the 7th field. Format: "MMDDhhmmss". In the hex format already.
        String F7 = get_F7(parsedMessage.getTransactionDate());
        System.out.println("F7 = " + F7);
        String F11 = get_F11(parsedMessage.getId());
        System.out.println("F11 = " + F11);
        String F37 = get_F37 (parsedMessage.getTransactionDate(), F7, F11);
        System.out.println("F37 = " + F37);
        // Compulsory fields: the 7th, 11th, 37th, 63th (the 2th subfield).
        parsedMessage.setHex("01000220000008000002" + F7 + F11 + F37);
        parsedMessage.setEdited(true);

        System.out.println(parsedMessage.getHex());
    }

    /*
    Returns the transactionDate as a String in the format of the 7th field.
    */
    private static String get_F7(LocalDateTime transactionDate) {
        // Formation of the 7th field. Format: "MMDDhhmmss".
        int month = transactionDate.getMonth().getValue();
        int day = transactionDate.getDayOfMonth();
        int hours = transactionDate.getHour();
        int minutes = transactionDate.getMinute();
        int seconds = transactionDate.getSecond();
        String monthStr = getStringOfDateComponentForField_7(month);
        String dayStr = getStringOfDateComponentForField_7(day);
        String hoursStr = getStringOfDateComponentForField_7(hours);
        String minutesStr = getStringOfDateComponentForField_7(minutes);
        String secondsStr = getStringOfDateComponentForField_7(seconds);
        String F7_Str = monthStr + dayStr + hoursStr + minutesStr + secondsStr;
        return F7_Str;
    }

    /*
    Returns the value of the transmitted date component in the String format for the 7 field.
    If the length of the initial value is not enough, the zero is additional to the beginning to get two symbols.
     */
    private static String getStringOfDateComponentForField_7(int component) {
        String componentStr = String.valueOf(component);
        if (componentStr.length() == 2)
            return componentStr;
        return "0" + componentStr;
    }

    /*
    Returns the globalTransactionNumber as a String in the format of the 11th field.
    */
    private static String get_F11(int id) {
        // Suggested that >= 0.
        if (id < 1)
            id++;
        int globalTransactionNumber = id % 1000000; // The length of the 11th field = 6.
        StringBuilder globalTransactionNumberStr = new StringBuilder();
        for (int i = 0; i < 6 - String.valueOf(globalTransactionNumber).length(); i++)
            globalTransactionNumberStr.append(0);
        globalTransactionNumberStr.append(globalTransactionNumber);
        return globalTransactionNumberStr.toString();
    }

    /*
    Returns the Retrieval Reference Number as a String in the format of the 37 field.
    The 37th field based on the transactionDate, F7 and F11.
     */
    private static String get_F37 (LocalDateTime transactionDate, String F7, String F11) {
        // Format: "YJJJHHNNNNNN".
        StringBuilder retrievalReferenceNumber = new StringBuilder();
        // Add "Y".
        String year = String.valueOf(transactionDate.getYear());
        char lastNumOfYear = year.charAt(year.length() - 1);
        retrievalReferenceNumber.append(lastNumOfYear);
        // Add "JJJ".
        StringBuilder dayNumInYear = new StringBuilder(String.valueOf(transactionDate.getDayOfYear()));
        while (dayNumInYear.length() < 3)
            dayNumInYear.insert(0, 0);
        retrievalReferenceNumber.append(dayNumInYear);
        // Add "HH".
        retrievalReferenceNumber.append(F7.substring(4, 6));
        // Add "NNNNNN".
        retrievalReferenceNumber.append(F11);
        return retrievalReferenceNumber.toString();
    }
}
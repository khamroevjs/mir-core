package mir.change;

import mir.models.ParsedField;
import mir.models.ParsedMessage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;

public class Changer {

    static int currentGlobalMIPTransactionNumber = 1;
    static LocalDate currentParsedMessageDate = LocalDate.now();

    /*
    Sets: transactionDate, edited (as true).
    Adds the 7th, 11th, 37th fields to the parsedMessage.
    // todo: add to the documentation.
    Note, [0100] or [0400] ar expected.
    */
    public static ParsedMessage completeParsedMessageRequest(ParsedMessage parsedMessage) {
        // Getting of the transactionDate.
        LocalDateTime transactionDate = LocalDateTime.now();
        parsedMessage.setTransactionDate(transactionDate);
        // Getting of the contents of the additional fields.
        String F7_content =  get_F7_content(transactionDate);
        String F11_content = get_F11_content(LocalDate.of(transactionDate.getYear(), transactionDate.getMonth(),
                transactionDate.getDayOfMonth()));
        String F37_content = get_F37_content(transactionDate, F7_content, F11_content);
        // Getting of the parsed fields.
        ParsedField F7 = get_F7(F7_content);
        ParsedField F11 = get_F11(F11_content);
        ParsedField F37 = get_F37(F37_content);
        // Adding of the parsed fields.
        HashMap<Integer, ParsedField> fields = parsedMessage.getFields();
        fields.put(7, F7);
        fields.put(11, F11);
        fields.put(37, F37);
        parsedMessage.setFields(fields);
        // Setting of the edited.
        parsedMessage.setEdited(true);
        return parsedMessage;
    }

    /*
    Returns the transactionDate as a String in the format of the 7th field.
    */
    static String get_F7_content(LocalDateTime transactionDate) {
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
    static String getStringOfDateComponentForField_7(int component) {
        String componentStr = String.valueOf(component);
        if (componentStr.length() == 2)
            return componentStr;
        return "0" + componentStr;
    }

    /*
    Returns the globalTransactionNumber as a String in the format of the 11th field.
    */
    static String get_F11_content(LocalDate parsedMessageDate) {
        int newGlobalMIPTransactionNumber;
        // The same day. Index of the 11th field is next the last.
        if (currentParsedMessageDate.compareTo(parsedMessageDate) == 0) {
            // todo: add to the documentation if time will be.
            // The length of 11th field = 6.
            if (currentGlobalMIPTransactionNumber > 999999)
                // +1 to do not get zero index.
                currentGlobalMIPTransactionNumber = (currentGlobalMIPTransactionNumber + 1) % 1000000;
            newGlobalMIPTransactionNumber = currentGlobalMIPTransactionNumber;
            currentGlobalMIPTransactionNumber++;
        }
        // A day after the last. Index of the 11th field is starting at the beginning.
        else {
            currentGlobalMIPTransactionNumber = 1;
            newGlobalMIPTransactionNumber = currentGlobalMIPTransactionNumber;
            currentParsedMessageDate = parsedMessageDate;
        }
        StringBuilder globalTransactionNumberStr = new StringBuilder();
        for (int i = 0; i < 6 - String.valueOf(newGlobalMIPTransactionNumber).length(); i++)
            globalTransactionNumberStr.append(0);
        globalTransactionNumberStr.append(newGlobalMIPTransactionNumber);
        return globalTransactionNumberStr.toString();
    }

    /*
    Returns the Retrieval Reference Number as a String in the format of the 37 field.
    The 37th field based on the transactionDate, F7 and F11.
    */
    static String get_F37_content(LocalDateTime transactionDate, String F7, String F11) {
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

    static ParsedField get_F7(String content) {
        ParsedField parsedField = new ParsedField();
        parsedField.setId(7);
        parsedField.setType("n");
        parsedField.setLengthMIP(10);
        parsedField.setContent(content);
        return parsedField;
    }

    static ParsedField get_F11(String content) {
        ParsedField parsedField = new ParsedField();
        parsedField.setId(11);
        parsedField.setType("n");
        parsedField.setLengthMIP(6);
        parsedField.setContent(content);
        return parsedField;
    }

    static ParsedField get_F37(String content) {
        ParsedField parsedField = new ParsedField();
        parsedField.setId(37);
        parsedField.setType("n");
        parsedField.setLengthMIP(12);
        parsedField.setContent(content);
        return parsedField;
    }
}

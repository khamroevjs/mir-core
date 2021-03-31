package mir.change;

import mir.models.ParsedMessage;

public class Changer {

    public static ParsedMessage formResponse(ParsedMessage parsedMessage) {
        if (parsedMessage.getMti().compareTo("0100") != 0 &&
            parsedMessage.getMti().compareTo("0400") != 0)
            // Todo: add to the documentation.
            throw new IllegalArgumentException("0100 or 0400 message were expected!");
        // Change the mti.
        parsedMessage.setMti(getMti(parsedMessage));
        parsedMessage.setEdited(true);
        parsedMessage = deleteExtraParsedFields(parsedMessage);
        return parsedMessage;
    }

    private static String getMti(ParsedMessage parsedMessage) {
        if (parsedMessage.getMti().compareTo("0100") == 0)
            return "0110";
        // parsedMessage.getMti().compareTo("0400") == 0
        return "0410";
    }


    /*
    // Todo: add to the documentation.
    Note, there only [0100] or [0400] messages are expected.
    So, the 12th, 13th, 23th fields are deleted.
    */
    private static ParsedMessage deleteExtraParsedFields(ParsedMessage parsedMessage) {
        parsedMessage.getFields().remove(12);
        parsedMessage.getFields().remove(13);
        parsedMessage.getFields().remove(23);
        return parsedMessage;
    }
}

package mir.parsing.formatters;

import com.imohsenb.ISO8583.builders.ISOMessageBuilder;
import com.imohsenb.ISO8583.entities.ISOMessage;
import com.imohsenb.ISO8583.enums.FIELDS;
import com.imohsenb.ISO8583.enums.SUBFIELDS;
import com.imohsenb.ISO8583.exceptions.ISOException;
import com.imohsenb.ISO8583.utils.StringUtil;
import mir.models.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeMap;

public class Encoder {

    /*
    Converts the hex into the ParsedMessage.
     */
    public ParsedMessage getParsedMessageFromEncodedMessage(String hex) throws ISOException {
        // Transformation of the hex to the isoMessage.
        ISOMessage isoMessage = ISOMessageBuilder.Unpacker()
                .setMessage(hex)
                .build();
        // Transformation of the parsedMessage into the getParsedMessage.
        ParsedMessage parsedMessage = getParsedMessageFromISO(isoMessage);
        return parsedMessage;
    }

    /*
    Returns the encodedMessage the folders of which match to
    the values of the transmitted parsedMessage.
    Accepts a parsedMessage.
    */
    public String getEncodedMessageFromParsedMessage(ParsedMessage parsedMessage) throws ISOException {
        if (!parsedMessage.getEdited())
            return parsedMessage.getHex();
        StringBuilder message = new StringBuilder();
        message.append(parsedMessage.getMti());
        // Formation of the primaryBitmap and extracting of the hex bodies of fields into the content.
        byte[] primaryBitmap = new byte[64];
        StringBuilder content = new StringBuilder();
        HashMap<Integer, ParsedField> parsedFields = parsedMessage.getFields();
        Integer[] ids = Arrays.copyOf(parsedFields.keySet().toArray(), parsedFields.keySet().size(), Integer[].class);
        Arrays.sort(ids);
        for (int id : ids) {
            ParsedField parsedField = parsedFields.get(id);
            // Marking of the bit.
            primaryBitmap[id - 1] = 1;
            // Addition of the length prefix.
            FIELDS field = FIELDS.valueOf(parsedField.getId());
            if (!field.isFixed())
                content.append(getLengthMIPOfParsedFieldForHex(parsedField));
            // Addition of the body or elements.
            content.append(getHexContentOfParsedField(parsedField));
        }
        String primaryBitmapHexStr = convertBinPrimaryBitmapToHexStr(primaryBitmap);
        // The transformation of the bits array into the byte array and then into the hex String.
        message.append(primaryBitmapHexStr);
        message.append(content);
        return message.toString();
    }

    /*
    Returns the lengthMIP of parsedField in the format necessary for the hex.
    */
    static String getLengthMIPOfParsedFieldForHex (ParsedField parsedField) {
        StringBuilder lengthMIPForHex = new StringBuilder();
        String lengthMIPStr = String.valueOf(parsedField.getLengthMIP());
        String lengthFormat = FIELDS.valueOf(parsedField.getId()).getFormat();
        if (lengthFormat.compareTo("LL") == 0)
            while (lengthMIPStr.length() + lengthMIPForHex.length() < 2)
                lengthMIPForHex.append("0");
            // lengthFormat.compareTo("LLL") == 0
        else
            while (lengthMIPStr.length() + lengthMIPForHex.length() < 4)
                lengthMIPForHex.append("0");
        lengthMIPForHex.append(lengthMIPStr);
        return lengthMIPForHex.toString();
    }

    /*
    Returns the content of the parsedField in the hex format.
    // Todo: add this feature to the documentation.
    Note, unfixed fields with subfields are not considered!
    */
    static String getHexContentOfParsedField(ParsedField parsedField) {
        if (parsedField.getHasSubfields())
            return getHexContentOfParsedSubfields(parsedField);
        if (parsedField.getHasElements())
            return getHexContentOfParsedElements(parsedField);
        // The parsedField has not some subfields and elements.
        // Compressed format.
        if (parsedField.getType().compareTo("n") == 0 || parsedField.getType().compareTo("b") == 0)
            return parsedField.getContent();
            // Uncompressed format.
        else
            return StringUtil.asciiToHex(parsedField.getContent());
    }

    /*
    Returns the content of the parsed subfields of the parsedField in the hex format.
    // Todo: add this feature to the documentation.
    Note, unfixed fields with subfields are not considered!
    */
    static String getHexContentOfParsedSubfields(ParsedField parsedField) {
        StringBuilder contentHex = new StringBuilder();
        HashMap<Integer, ParsedSubfield> subfields = parsedField.getSubfields();
        Integer[] subfieldsIds = Arrays.copyOf(subfields.keySet().toArray(), subfields.keySet().size(), Integer[].class);
        Arrays.sort(subfieldsIds);
        for (int subfieldId : subfieldsIds) {
            ParsedSubfield parsedSubfield = subfields.get(subfieldId);
            // Add the content.
            // Compressed format.
            if (parsedSubfield.getType().compareTo("n") == 0 || parsedSubfield.getType().compareTo("b") == 0)
                contentHex.append(parsedSubfield.getContent());
                // Uncompressed format.
            else
                contentHex.append(StringUtil.asciiToHex(parsedSubfield.getContent()));
        }
        return contentHex.toString();
    }

    /*
    Returns the content of the parsed elements of the parsedField in the hex format.
    */
    static String getHexContentOfParsedElements(ParsedField parsedField) {
        StringBuilder contentHex = new StringBuilder();
        HashMap<Integer, ParsedElement> elements = parsedField.getElements();
        Integer[] elementsIds = Arrays.copyOf(elements.keySet().toArray(), elements.keySet().size(), Integer[].class);
        Arrays.sort(elementsIds);
        for (int elemId : elementsIds) {
            ParsedElement parsedElement = elements.get(elemId);
            // Add the type.
            contentHex.append(StringUtil.asciiToHex(parsedElement.getType()));
            // Add the id.
            contentHex.append(StringUtil.asciiToHex(String.valueOf(parsedElement.getHexId())));
            // Add the length.
            contentHex.append(StringUtil.asciiToHex(String.valueOf(parsedElement.getHexLengthMIP())));
            // Add the content.
            // Compressed format.
            if (parsedElement.getType().compareTo("%") == 0)
                contentHex.append(parsedElement.getContent());
                // Uncompressed format.
            else
                contentHex.append(StringUtil.asciiToHex(parsedElement.getContent()));
        }
        return contentHex.toString();
    }

    static String convertBinPrimaryBitmapToHexStr(byte[] primaryBitmapBin) {
        StringBuilder primaryBitmapHex = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            StringBuilder bin = new StringBuilder();
            bin.append(primaryBitmapBin[i * 4]);
            bin.append(primaryBitmapBin[i * 4 + 1]);
            bin.append(primaryBitmapBin[i * 4 + 2]);
            bin.append(primaryBitmapBin[i * 4 + 3]);
            primaryBitmapHex.append(convertBinToHex(bin.toString()));
        }
        return primaryBitmapHex.toString();
    }

    /*
    Converts an ISOMessage into a ParsedMessage.
    */
    static ParsedMessage getParsedMessageFromISO(ISOMessage isoMessage) throws ISOException {
        ParsedMessage parsedMessage = new ParsedMessage();
        parsedMessage.setMti(isoMessage.getMti());
        parsedMessage.setHex(StringUtil.fromByteArray(isoMessage.getBody()));
        TreeMap<Integer, byte[]> fields = isoMessage.getFields();
        for (int fieldId : fields.keySet()) {
            ParsedField parsedField = new ParsedField();
            // Setting of the id.
            parsedField.setId(fieldId);
            FIELDS field = FIELDS.valueOf(fieldId);
            // Setting of the type.
            parsedField.setType(field.getType());
            // Setting of the content.
            parsedField.setContent(isoMessage.getStringField(fieldId, true));
            parsedField.setSubfields(getContentOfSubfields(parsedField));
            parsedField.setElements(getContentOfElements(parsedField));
            // Setting of the lengthMIP.
            parsedField.setLengthMIP(getLengthMIPOfParsedField(isoMessage, parsedField));

            parsedMessage.addField(parsedField);
        }
        // Setting of the transaction number must happen after the parsing of fields
        // because this value is taken from one of fields.
        parsedMessage.setTransactionNumber(getTransactionNumber(parsedMessage));
        return parsedMessage;
    }


    static int getLengthMIPOfParsedField(ISOMessage isoMessage, ParsedField parsedField) {
        if (!parsedField.getHasSubfields() && !parsedField.getHasElements()) {
            int fieldId = parsedField.getId();
            FIELDS field = FIELDS.valueOf(fieldId);
            if (field.isFixed())
                return field.getMaxLength();
            else
                return isoMessage.getFieldLength(fieldId);
        }
        int length = 0;
        if (parsedField.getHasSubfields()) {
            for (ParsedSubfield parsedSubfield : parsedField.getSubfields().values())
                length += parsedSubfield.getLengthMIP();
        }
        // parsedField.getHasElements()
        else {
            for (ParsedElement parsedElement : parsedField.getElements().values())
                // type + id + length + content.
                length += 1 + 2 + 2 + parsedElement.getLengthMIP();
        }
        return length;
    }

    // Todo: check that this method can be removed and remove it.
    /*private static int getLengthRealBytesForParsedField(ISOMessage isoMessage, ParsedField parsedField, FIELDS field) {
        int length;
        if (!parsedField.getHasSubfields() && !parsedField.getHasElements())
            length = getLengthRealBytesForParsedFieldWithoutSubfieldsOrElements(isoMessage, parsedField, field);
        else {
            length = 0;
            // TODO: add to the documentation.
            // Only fixed fields are considered!
            if (parsedField.getHasSubfields())
                for (ParsedSubfield parsedSubfield : parsedField.getSubfields().values())
                    length += parsedSubfield.getContent().length();
            // parsedField.getHasElements()
            else {
                for (ParsedElement parsedElement : parsedField.getElements().values())
                    length += parsedElement.getContent().length();
            }
        }
        return length;
    }*/

    // Todo: check that this method can be removed and remove it.
    /*private static int getLengthRealBytesForParsedFieldWithoutSubfieldsOrElements
            (ISOMessage isoMessage, ParsedField parsedField, FIELDS field) {
        int length;
        // Getting of the length from FIELDS.
        if (field.isFixed()) {
            // In compressed format.
            if (field.getType().compareTo("n") == 0 ||
                field.getType().compareTo("b") == 0)
                // +1 to consider the first additional zero.
                length = (field.getMaxLength() + 1) / 2;
            else
                length = field.getMaxLength();
        }
        // field is unfixed.
        // Getting of the length from  the lengths of the isoMessage.
        else {
            // In compressed format.
            if (field.getType().compareTo("n") == 0 ||
                field.getType().compareTo("b") == 0)
                // +1 to consider the first additional zero.
                length = (isoMessage.getFieldLength(parsedField.getId()) + 1) / 2;
            else
                length = isoMessage.getFieldLength(parsedField.getId());
        }
        return length;
    }*/

    /*
    If the transmitted field has subfields, this method sets its parsed subfields to this.
    The sequence of the bodies of the subfields are setting as the content of the parsedField.
    Note, in this moment the conversion of the parsedField content from the hex format is happening.
    */
    static HashMap<Integer, ParsedSubfield> getContentOfSubfields(ParsedField parsedField) throws ISOException {
        int fieldId = parsedField.getId();
        FIELDS fields = FIELDS.valueOf(fieldId);
        HashMap<Integer, ParsedSubfield> subfields = new HashMap<>();
        // The field has subfields.
        if (fields.getHasSubfields()) {
            parsedField.setHasSubfields(true);
            subfields = parseSubfields(parsedField);
        }
        return subfields;
    }

    static HashMap<Integer, ParsedElement> getContentOfElements(ParsedField parsedField) throws ISOException {
        int fieldId = parsedField.getId();
        FIELDS fields = FIELDS.valueOf(fieldId);
        HashMap<Integer, ParsedElement> elements = parsedField.getElements();
        // The field has elements.
        if (fields.getHasElements()) {
            parsedField.setHasElements(true);
            elements = parseElements(parsedField);
        }
        return elements;
    }

    // TOdO: I really don't want to remove this but there is not a real reason to use this functional,
    //  only as pleasant feature. But then there is an additional work to insert this code into the current.
    /*
    Sets the sequence of the subfields contents as the content of the parsedField.
    */
    /*private static void setSubfieldsAsParsedFieldContent(ParsedField parsedField) {
        StringBuilder fieldContent = new StringBuilder();
        HashMap<Integer, ParsedSubfield> subfields = parsedField.getSubfields();
        for (ParsedSubfield subfield : subfields.values())
            fieldContent.append(subfield.getContent());
        parsedField.setContent(fieldContent.toString());
        return;
    }*/

    /*
    Sets the sequence of the elements contents as the content of the parsedField.
    */
    /*private static void setElementsAsParsedFieldContent(ParsedField parsedField) {
        StringBuilder fieldContent = new StringBuilder();
        HashMap<Integer, ParsedElement> elements = parsedField.getElements();
        for (ParsedElement elem : elements.values()) {
            fieldContent.append(elem.getType());
            fieldContent.append(elem.getHexId());
            fieldContent.append(elem.getHexLengthMIP());
            fieldContent.append(elem.getContent());
        }
        parsedField.setContent(fieldContent.toString());
        return;
    }*/
    // Todo: the end.

    /*
    Returns the HashMap of the elements of the field if it has these.
    The content of the parsedField is represented in hex format.
    */
    static HashMap<Integer, ParsedElement> parseElements(ParsedField parsedField) throws ISOException {
        HashMap<Integer, ParsedElement> elements = new HashMap<Integer, ParsedElement>();
        String fieldContent = parsedField.getContent();
        // Formation of the parsed elements.
        int indSym = 0;
        while (indSym < fieldContent.length()) {
            ParsedElement parsedElement = new ParsedElement();
            // The type of an parsedElement takes positions 0-1.
            String typeHex = fieldContent.substring(indSym, indSym + 2);
            parsedElement.setType(StringUtil.hexToAscii(typeHex));
            // The id of an parsedElement takes positions 2-5.
            String id = fieldContent.substring(indSym + 2, indSym + 6);
            parsedElement.setId(Integer.parseInt(StringUtil.hexToAscii(id), 16));
            // The length of an parsedElement takes positions 6-9.
            String lengthHex = fieldContent.substring(indSym + 6, indSym + 10);
            parsedElement.setLengthMIP(Integer.parseInt(StringUtil.hexToAscii(lengthHex), 16));
            // The real length.
            int contentLengthInSymbols = getLengthInSymbolsOfElem(parsedElement);
            // Setting of the content.
            parsedElement.setContent(getElemContent(parsedField, parsedElement, indSym, contentLengthInSymbols));
            // The offset of the indSym to make it the first index of the next parsedElement.
            elements.put(parsedElement.getId(), parsedElement);
            indSym += 10 + contentLengthInSymbols;
        }
        return elements;
    }

    /*
    Returns the real length (the quantity of symbols) of the content of the transmitted parsedElement.
    The first additional zero is considered.
    */
    static int getLengthInSymbolsOfElem(ParsedElement parsedElement) {
        int elemLength = parsedElement.getLengthMIP();
        // Compressed format.
        if (parsedElement.getType().compareTo("%") == 0) {
            // The first additional zero is considered.
            if (elemLength % 2 != 0)
                elemLength++;
        }
        // Uncompressed format.
        // parsedElement.getType().compareTo("^") == 0.
        else
            elemLength *= 2; // Every element takes 2 hexadecimal symbols.
        return elemLength;
    }

    /*
    Returns the content of the transmitted element.
    If the type of this is equal to "^", conversion from hex happens.
    In otherwise the conversion does not happen.
    */
    static String getElemContent
    (ParsedField parsedField, ParsedElement parsedElement, int indSym, int contentSymbolsRealCount) {
        String fieldContent = parsedField.getContent();
        // The content of an parsedElement takes positions begin at the 10th.
        String elemContent = fieldContent.substring(indSym + 10, indSym + 10 + contentSymbolsRealCount);
        // The content of the parsedElement has been converted from the hex format already.
        if (parsedElement.getType().compareTo("%") == 0)
            return elemContent;
        // The content of the parsedElement needs in the conversion from the hex format.
        return StringUtil.hexToAscii(elemContent);
    }

    /*
    Returns the subfields which contains parsed subfields.
    This method does not consider unfixed parsed fields!
    */
    static HashMap<Integer, ParsedSubfield> parseSubfields(ParsedField parsedField)
            throws StringIndexOutOfBoundsException, ISOException {
        HashMap<Integer, ParsedSubfield> subfields = new HashMap<Integer, ParsedSubfield>();
        // Formation of the parsed subfields.
        int fieldId = parsedField.getId();
        int maxCountSubfields = FIELDS.valueOf(fieldId).getMaxSubfieldsId();
        for (int subfieldId = 1; subfieldId <= maxCountSubfields; subfieldId++)  {
            ParsedSubfield parsedSubfield = formParsedSubfield(parsedField, subfieldId);
            subfields.put(subfieldId, parsedSubfield);
        }
        return subfields;
    }

    /*
    Returns the formed parsedSubfield.
    This method does not consider unfixed parsed fields!
    */
    static ParsedSubfield formParsedSubfield(ParsedField parsedField, int subfieldId) throws ISOException {
        int fieldId = parsedField.getId();
        SUBFIELDS subfieldSample = SUBFIELDS.valueOf(fieldId, subfieldId);
        if (subfieldSample == null)
            throw new ISOException("The information about the subfield №" + subfieldId +
                    " of the field №" + fieldId + " is not provided by the Lib" +
                    " on the strength of the project features or because the MIP does not suggest this!");
        ParsedSubfield parsedSubfield = new ParsedSubfield();
        parsedSubfield.setId(subfieldId);
        parsedSubfield.setType(subfieldSample.getType());
        // The length according to the MIP.
        // The subfields with the variable length is not taken into account!!!
        int length = subfieldSample.getLength();
        parsedSubfield.setLengthMIP(length);
        // The real length (the quantity of symbols) of the parsedSubfield.
        length = getParsedSubfieldRealLength(parsedField, parsedSubfield, length);
        parsedSubfield.setContent(getSubfieldContent(parsedField, subfieldSample, length));
        return parsedSubfield;
    }

    static String getSubfieldContent
            (ParsedField parsedField, SUBFIELDS subfieldSample, int lengthRealOfParsedSubfield) {
        int beginInd = subfieldSample.getBeginInd();
        // The content of the subfield has been converted from the hex format already.
        if (subfieldSample.getType().compareTo("n") == 0 ||
                subfieldSample.getType().compareTo("b") == 0)
            return parsedField.getContent().substring(beginInd, beginInd + lengthRealOfParsedSubfield);
            // The content of the subfield needs in the conversion from the hex format.
        else {
            String subfieldHexContent
                    = parsedField.getContent().substring(beginInd, beginInd + lengthRealOfParsedSubfield);
            return StringUtil.hexToAscii(subfieldHexContent);
        }
    }

    /*
    Returns the real length (the quantity of symbols) of the transmitted parsedSubfield.
    */
    static int getParsedSubfieldRealLength
    (ParsedField parsedField, ParsedSubfield parsedSubfield, int length) {
        // Compressed format.
        if (parsedSubfield.getType().compareTo("n") == 0 ||
                parsedSubfield.getType().compareTo("b") == 0) {
            // The first additional zero is considered.
            if (length % 2 != 0)
                length++;
        }
        // Uncompressed format.
        else
            length *= 2;
        return length;
    }

    // Todo: change description in the documentation if it is necessary:
    //  Here the using field is changed. the 2th subfield of the 63th field -> the 37th field.
    /*
    Returns the transaction number of the parsedMessage if it has the 37th field.
    Else returns null.
    */
    static String getTransactionNumber(ParsedMessage parsedMessage) {
        int fieldId = 37;
        // If the required field is set.
        if (parsedMessage.getFields().containsKey(fieldId))
            return parsedMessage.getFields().get(fieldId).getContent();
        return null;
    }

    /*
    Accepts a binary number which has 4 ranks and converts into a hex number with one rank.
    */
    static char convertBinToHex(String numBin) {
        switch(numBin) {
            case "0000":
                return '0';
            case "0001":
                return '1';
            case "0010":
                return '2';
            case "0011":
                return '3';
            case "0100":
                return '4';
            case "0101":
                return '5';
            case "0110":
                return '6';
            case "0111":
                return '7';
            case "1000":
                return '8';
            case "1001":
                return '9';
            case "1010":
                return 'A';
            case "1011":
                return 'B';
            case "1100":
                return 'C';
            case "1101":
                return 'D';
            case "1110":
                return 'E';
            case "1111":
                return 'F';
            default:
                return 'Z';
        }
    }
}

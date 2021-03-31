package mir.check;

import com.imohsenb.ISO8583.enums.ELEMENTS;
import com.imohsenb.ISO8583.enums.FIELDS;
import com.imohsenb.ISO8583.enums.SUBFIELDS;
import mir.models.MessageError;
import mir.models.ParsedElement;
import mir.models.ParsedField;
import mir.models.ParsedMessage;
import mir.models.ParsedSubfield;
import mir.models.check_annotations.AnyOf;
import javax.validation.constraints.Pattern;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.regex.Matcher;

public class Checker {

    public static List<MessageError> checkParsedMessage
            (ParsedMessage parsedMessage) throws NoSuchFieldException, IllegalAccessException {
        List<MessageError> errors = new ArrayList<>();
        errors = checkLengthAndTypeOfPrimaryBitmap(parsedMessage, errors);
        // The check of the presence of the errors of the primary bitmap.
        // If they are there, the further check won't happen.
        for (MessageError error : errors) {
            String errorMessage = error.getMessage();
            // An incorrect length.
            if (errorMessage.compareTo
                    ("The message has not enough symbols to contain the primary bitmap!") == 0 ||
                    // An incorrect type.
                    errorMessage.compareTo
                            ("The primary bitmap must be represented by only the hexadecimal numbers!") == 0)
                return errors;
        }
        // The primary bitmap is correct.
        errors = checkFieldsOnAnnotations(parsedMessage, errors);
        errors = checkTransactionDate(parsedMessage, errors);
        errors = checkParsedFields(parsedMessage, errors);
        return errors;
    }

    /*
    Checks of the parsed fields of the transmitted parsedMessage
    on the correctness of annotations.
     */
    private static List<MessageError> checkFieldsOnAnnotations
    (ParsedMessage parsedMessage, List<MessageError> errors) throws IllegalAccessException {
        Class parsedMessageCLass = parsedMessage.getClass();
        Field[] fields = parsedMessageCLass.getDeclaredFields();
        for (Field field : fields) {
            // Check of the field annotations.
            AnnotatedType annotatedType = field.getAnnotatedType();
            Annotation[] annotations = annotatedType.getAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation instanceof AnyOf)
                    errors = checkAnyOfAnnotation(parsedMessage, field, errors);
                else
                if (annotation instanceof Pattern)
                    errors = checkPatternAnnotation(parsedMessage, field, errors);
            }
        }
        return errors;
    }

    private static List<MessageError> checkAnyOfAnnotation
            (Object object, Field field, List<MessageError> errors) throws IllegalAccessException {
        // Getting of the @AnyOf annotation.
        AnnotatedType annotatedType = field.getAnnotatedType();
        AnyOf anyOfAnnotation = annotatedType.getDeclaredAnnotation(AnyOf.class);
        // Getting of the field value.
        field.setAccessible(true);
        Object fieldObject = field.get(object);
        // The fieldObject is String.
        if (fieldObject instanceof String) {
            String fieldObjectStr = (String)fieldObject;
            for (Object objectElem : anyOfAnnotation.values())
                if (((String)objectElem).compareTo(fieldObjectStr) == 0)
                    return errors;
        }
        else
            // The fieldObject is Integer.
            if (fieldObject instanceof Integer) {
                Integer fieldObjectInt = (Integer)fieldObject;
                for (Object objectElem : anyOfAnnotation.values())
                    if (Integer.parseInt((String)objectElem) == fieldObjectInt)
                        return errors;
            }
            // The fieldObject is some Object.
            else {
                for (Object objectElem : anyOfAnnotation.values())
                    if (objectElem.equals(fieldObject))
                        return errors;
            }
        String fieldName = field.getName();
        errors.add(new MessageError("The value of the " + fieldName +
                " of the " + object.getClass().getSimpleName() +
                " is not one from the allowable set!"));
        return errors;
    }

    // Todo: add to the documentation (if time is enough) that we do no this the cehck of @Pattern annotation
    //  but provide the opportunities to check it if it is needed.
    /*
    Checks the match of the field value with the pattern from the @Pattern annotation.
     */
    private static List<MessageError> checkPatternAnnotation
    (Object object, Field field, List<MessageError> errors) throws IllegalAccessException {
        // Getting of the @Pattern annotation.
        AnnotatedType annotatedType = field.getAnnotatedType();
        Pattern patternAnnotation = annotatedType.getDeclaredAnnotation(Pattern.class);
        // Getting of the field value.
        field.setAccessible(true);
        Object fieldObject = field.get(object);
        if (fieldObject == null) {
            errors.add(new MessageError("The field " + field.getName() + " of the parsedMessage must not be null!"));
            return  errors;
        }
        // Pattern.
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(patternAnnotation.regexp());
        Matcher matcher = pattern.matcher((String)fieldObject);
        // The value does not match to the pattern.
        if (!matcher.matches()) {
            String fieldName = field.getName();
            errors.add(new MessageError("The " + fieldName + " of the " +
                    object.getClass().getSimpleName() + "has unallowable value!"));
        }
        return errors;
    }

    /*
    Checks that the date nad time of the transaction are earlier than the moment of the checking.
     */
    private static List<MessageError> checkTransactionDate(ParsedMessage parsedMessage, List<MessageError> errors) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime transactionDate = parsedMessage.getTransactionDate();
        if (transactionDate == null)
            errors.add(new MessageError("The date and time of the parsedMessage is missed!"));
        else
        if (transactionDate.compareTo(now) == 0)
            errors.add(new MessageError("The date and time of the transaction must be earlier!"));
        return errors;
    }

    /*
    Checks the primary bitmap of the hex of the transmitted parsedMessage
    on the using only the hexadecimal numbers.
     */
    private static List<MessageError> checkLengthAndTypeOfPrimaryBitmap
    (ParsedMessage parsedMessage, List<MessageError> errors) {
        String hex = parsedMessage.getHex();
        // The quantity of symbols is not enough for the representation of the primary bitmap.
        if (hex.length() < 20) {
            errors.add(new MessageError("The message has not enough symbols to contain the primary bitmap!"));
            return errors;
        }
        // The quantity of symbols is enough for the representation of the primary bitmap.
        // Check of the correctness of the 16 symbols representing the primary bitmap.
        String primaryBitmap = hex.substring(4, 20);
        // The first field value equaling 0 means that there are only the primary bitmap (no the secondary bitmap).
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("[0-7].{15}");
        java.util.regex.Matcher matcher = pattern.matcher(primaryBitmap);
        if (!matcher.matches())
            // The first number of the primary bitmap must not be more than 7.
            errors.add(new MessageError("The first field value of the primary bitmap must be equal to 0!"));
        pattern = java.util.regex.Pattern.compile("[0-7][0-9A-F]{15}");
        matcher = pattern.matcher(primaryBitmap);
        if (!matcher.matches())
            errors.add(new MessageError("The primary bitmap must be represented by only the hexadecimal numbers!"));
        return errors;
    }

    // TODO: set the conversion from hex to bin format.
    /*
    Checks that the primary bitmap has not except fields which are not foreseen by the Lib.
    Expects that the length and type of the symbols are correct.
    */
    /*private static List<MessageError> checkExceptFieldsOfPrimaryBitmap
    (ParsedMessage parsedMessage, List<MessageError> errors) {
        String hex = parsedMessage.getHex();
        String primaryBitmap = hex.substring(4, 20);
        for (int i = 0; i < 16; i++) {
            char hexSym = primaryBitmap.charAt(i);
            String fourBinSymbols = convertHexToBin(hexSym);
            for (int j = 0; j < 4; j++) {
                int fieldInd = fourBinSymbols.charAt(j);
                if (FIELDS.valueOf(fieldInd) == null)
                    errors.add(new MessageError("The field №" + fieldInd + " pointed out in the primary bitmap" +
                            " is not foreseen by the MIP!"));
            }
        }
        return errors;
    }*/

    /*
    Checks fields on correctness.
     */
    private static List<MessageError> checkParsedFields
    (ParsedMessage parsedMessage, List<MessageError> errors)
            throws IllegalAccessException, NoSuchFieldException {
        // MIP fields of the parsedMessage.
        HashMap<Integer, ParsedField> fields = parsedMessage.getFields();
        for (ParsedField parsedField : fields.values()) {
            // TODO: check that links below can be removed and these.
            /*int fieldId = parsedField.getId();
            if (FIELDS.valueOf(parsedField.getId()) == null) {
                errors.add(new MessageError("The information about the field №" + fieldId +
                        " is not provided by the Lib on the strength of" +
                        " the project features or because the MIP does not suggest this!"));
                continue;
            }*/
            // The information of the field is provided by the Lib.
            errors = checkAnyOfAnnotation(parsedField, parsedField.getClass().getDeclaredField("id"), errors);
            errors = checkTypeOfParsedField(parsedField, errors);
            errors = checkLengthOfParsedField(parsedField, errors);
            errors = checkHasSubfieldsFlag(parsedField, errors);
            errors = checkHasElementsFlag(parsedField, errors);
            errors = checkParsedSubfields(parsedField, errors);
            errors = checkParsedElements(parsedField, errors);
        }
        return errors;
    }

    /*
    Checks the type of the transmitted parsedField.
    The type field and the content field are checking.
    Here parsedField has id guaranteed provided by the Lib
    (due to previous parsing).
    */
    private static List<MessageError> checkTypeOfParsedField
    (ParsedField parsedField, List<MessageError> errors) {
        int id = parsedField.getId();
        // The right type for the field with this id according to MPI.
        String rightType = FIELDS.valueOf(id).getType();
        // The type set for the parsedField.
        String currentType = parsedField.getType();
        // Check of the type field.
        if (rightType.compareTo(currentType) != 0)
            errors.add(new MessageError("The type of the field №" + id + " is not correct!"));
        // Check of the match of the content field with the type.
        errors = checkTypeOfParsedFieldContent(id, parsedField, rightType, errors);
        return errors;
    }

    /*
    Checks the transmitted content of the parsedField on the match with
    the values allowable according to the type of the MIP field.
    */
    private static List<MessageError> checkTypeOfParsedFieldContent
    (int id, ParsedField parsedField, String rightType, List<MessageError> errors) {
        String content = parsedField.getContent();
        // Binary data represented by hexadecimal numbers.
        if (rightType.compareTo("b") == 0)
            errors = checkCertainTypeOfFieldContent(id, content, "[0-9A-F]+",
                    "hexadecimal numbers", errors);
        else {
            // Decimal numbers.
            if (rightType.compareTo("n") == 0)
                errors = checkCertainTypeOfFieldContent(id, content, "[0-9]+",
                        "decimal numbers", errors);
                // Decimal numbers, English letters, special symbols.
                // rightType.compareTo("ans") == 0.
            else
                errors = checkCertainTypeOfFieldContent(id, content, "[ -~]+",
                        "decimal, hexadecimal numbers, English letters or special symbols", errors);
        }
        return errors;
    }

    /*
    Checks the transmitted content of the parsedField on the match with
    the values allowable according to the type of the MIP field.
    The type is a certain known type.
     */
    private static List<MessageError> checkCertainTypeOfFieldContent
    (int id, String content, String allowableValuesPattern, String allowableValuesWords,
     List<MessageError> errors) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(allowableValuesPattern);
        java.util.regex.Matcher matcher = pattern.matcher(content);
        if (!matcher.matches())
            errors.add(new MessageError("The content \"" + content + "\" of the field with №" + id +
                    " has incorrect type of the data!" +
                    " There are only " + allowableValuesWords +" should be."));
        return errors;
    }

    /*
    Checks the length of the transmitted parsedField.
    The lengthMIP and length of the content of the field are checking.
    Note, the lengthReal is not considered because this is set during the parsing.
    */
    private static List<MessageError> checkLengthOfParsedField
    (ParsedField parsedField, List<MessageError> errors) {
        int id = parsedField.getId();
        FIELDS field = FIELDS.valueOf(id);
        // The boards of the right length for the field with this id according to MPI.
        int rightMinLengthMIP = field.getMinLength();
        int rightMaxLengthMIP = field.getMaxLength();
        // Comparison with the lengthMIP of the parsedField.
        int currentLengthMIP = parsedField.getLengthMIP();
        if (!(rightMinLengthMIP <= currentLengthMIP && currentLengthMIP <= rightMaxLengthMIP))
            errors.add(new MessageError("The length value of the field №" + id + " is not correct!"));
        // Check of the real length of the content of the parsedField.
        if (!field.getHasSubfields() && !field.getHasElements())
            errors = checkContentLengthOfParsedFieldWithoutSubfieldsAndElements
                    (parsedField, rightMinLengthMIP, rightMaxLengthMIP, errors);
        else {
            if (field.getHasSubfields())
                errors = checkContentLengthOfParsedFieldWithSubfields
                        (parsedField, rightMinLengthMIP, rightMaxLengthMIP, errors);
            // field.getHasElements().
            else
                errors = checkContentLengthOfParsedFieldWithElements
                        (parsedField, rightMinLengthMIP, rightMaxLengthMIP, errors);
        }
        return errors;
    }

    /*
    Here the content of the parsedField is converted from hex.
     */
    private static List<MessageError> checkContentLengthOfParsedFieldWithoutSubfieldsAndElements
            (ParsedField parsedField, int rightMinLengthMIP, int rightMaxLengthMIP, List<MessageError> errors) {
        int id = parsedField.getId();
        FIELDS field = FIELDS.valueOf(id);
        // Check of the real length of the content of the parsedField.
        int currentContentLength = parsedField.getContent().length();
        // The first additional zero is considered.
        if (parsedField.getType().compareTo("n") == 0 ||
            parsedField.getType().compareTo("b") == 0) {
            if (field.getMaxLength() % 2 != 0)
                currentContentLength -= 1;
        }
        if (!(rightMinLengthMIP <= currentContentLength && currentContentLength <= rightMaxLengthMIP))
            errors.add(new MessageError("The length of the content of the field №" + id + " is not correct!"));
        return errors;
    }

    /*
    Checks of the real length of the content of the parsedField with subfields.
    */
    private static List<MessageError> checkContentLengthOfParsedFieldWithSubfields
    (ParsedField parsedField, int rightMinLengthMIP, int rightMaxLengthMIP, List<MessageError> errors) {
        int fieldId = parsedField.getId();
        HashMap<Integer, ParsedSubfield> subfields = parsedField.getSubfields();
        // TODO: check that links below can be deleted.
        // Choice of only the subfields the information of which is provided by the Lib.
        /*HashMap<Integer, ParsedSubfield> knownSubfields = new HashMap<>();
        for (ParsedSubfield parsedSubfield : subfields.values()) {
            int subfieldId = parsedSubfield.getId();
            SUBFIELDS subfield = SUBFIELDS.valueOf(fieldId, subfieldId);
            if (subfield == null)
                errors.add(new MessageError("The subfield №" + subfieldId + " of the field №" + fieldId +
                        " is not provided by the Lib on the strength of the project " +
                        " or because the MIP does not suggest this!"));
            // The information about the parsedSubfield is provided by the Lib.
            else
                knownSubfields.put(subfieldId, subfields.get(subfieldId));
        }*/
        // TODO: the end.
        // Check of the real length of the content of the parsedField.
        int currentContentLengthMIP = getContentLengthMIPOfParsedFieldWithSubfields(parsedField, subfields);
        if (!(rightMinLengthMIP <= currentContentLengthMIP && currentContentLengthMIP <= rightMaxLengthMIP))
            errors.add(new MessageError("The content of the field №" + fieldId + "is not correct!"));
        return errors;
    }

    /*
    Returns the current length of the content of the parsedField with subfields.
    Note, here all the the subfields of the parsedField are provided by the Lib.
    */
    private static int getContentLengthMIPOfParsedFieldWithSubfields
    (ParsedField parsedField, HashMap<Integer, ParsedSubfield> knownSubfields) {
        int fieldId = parsedField.getId();
        int currentContentLength = 0;
        for (ParsedSubfield parsedSubfield : knownSubfields.values()) {
            int subfieldId = parsedSubfield.getId();
            SUBFIELDS subfield = SUBFIELDS.valueOf(fieldId, subfieldId);
            currentContentLength += getContentLengthMIPOfParsedSubfield(parsedSubfield, subfield);
        }
        return currentContentLength;
    }

    /*
    Check of the real length of the content of the parsedField with elements.
    */
    private static List<MessageError> checkContentLengthOfParsedFieldWithElements
    (ParsedField parsedField, int rightMinLengthMIP, int rightMaxLengthMIP, List<MessageError> errors) {
        int fieldId = parsedField.getId();
        HashMap<Integer, ParsedElement> elements = parsedField.getElements();
        // TODO : check that the links below can be removed.
        // The elements the information of which is provided by the Lib.
        /*HashMap<Integer, ParsedElement> knownElements = new HashMap<>();
        for (ParsedElement parsedElement : elements.values()) {
            int elemId = parsedElement.getId();
            ELEMENTS element = ELEMENTS.valueOf(fieldId, elemId);
            if (element == null)
                errors.add(new MessageError("The element №" + elemId + " of the field №" + fieldId +
                        " is not provided by the Lib on the strength of the project" +
                        " or because the MIP does not suggest this!"));
            // The information of the element is provided by the Lib.
            else
                knownElements.put(elemId, parsedElement);
        }*/
        // TODO: the end.
        int currentContentLength = getContentLengthMIPOfParsedFieldWithElements(elements);
        if (!(rightMinLengthMIP <= currentContentLength && currentContentLength <= rightMaxLengthMIP))
            errors.add(new MessageError("The content of the field №" + fieldId + "is not correct!"));
        return errors;
    }

    private static int getContentLengthMIPOfParsedFieldWithElements
            (HashMap<Integer, ParsedElement> knownElements) {
        int currentContentLength = 0;
        for (ParsedElement parsedElement : knownElements.values()) {
            // type + id + length + content.
            currentContentLength += 1 + 2 + 2 + getCurrentContentLengthMIPOfParsedElement(parsedElement);
        }
        return currentContentLength;
    }

    /*
    Checks the match between the value of the hasElements and the length of the elements hashmap.
     */
    private static List<MessageError> checkHasElementsFlag(ParsedField parsedField, List<MessageError> errors) {
        if (parsedField.getHasElements() == true && parsedField.getElements().size() == 0)
            errors.add(new MessageError("The field with №" + parsedField.getId() + "must have some elements!"));
        else
        if (parsedField.getHasElements() == false && parsedField.getElements().size() > 0)
            errors.add(new MessageError("The field with №" + parsedField.getId() + "must not have any elements!"));
        return errors;
    }

    /*
    Checks the match between the value of the hasSubfields and the length of the subfields hashmap.
     */
    private static List<MessageError> checkHasSubfieldsFlag(ParsedField parsedField, List<MessageError> errors) {
        if (parsedField.getHasSubfields() == true && parsedField.getSubfields().size() == 0)
            errors.add(new MessageError("The field with №" + parsedField.getId() + " must have some subfields!"));
        else
        if (parsedField.getHasSubfields() == false && parsedField.getSubfields().size() > 0)
            errors.add(new MessageError("The field with №" + parsedField.getId() + " must not have any subfields!"));
        return errors;
    }

    private static List<MessageError> checkParsedSubfields(ParsedField parsedField, List<MessageError> errors) {
        int fieldId = parsedField.getId();
        HashMap<Integer, ParsedSubfield> subfields = parsedField.getSubfields();
        for (ParsedSubfield parsedSubfield : subfields.values()) {
            int subfieldId = parsedSubfield.getId();
            SUBFIELDS subfieldSample = SUBFIELDS.valueOf(fieldId, subfieldId);
            // TODO: add the feature below to the documentation.
            if (subfieldSample == null)
                errors.add(new MessageError("The subfields of the field №" + fieldId +
                        " is not provided by the Lib on the strength of the project" +
                        " or because the MIP does not suggest this!"));
            // The information about the parsedSubfield is provided by the Lib.
            else {
                checkTypeOfParsedSubfield(parsedField, parsedSubfield, subfieldSample, errors);
                // TODO: add to the documentation that only fixed subfields are considered!
                checkLengthOfParsedSubfield(parsedField, parsedSubfield, errors);
            }
        }
        return errors;
    }

    /*
    Note, here the all subfields are provided by the Lib.
     */
    private static List<MessageError> checkTypeOfParsedSubfield
    (ParsedField parsedField, ParsedSubfield parsedSubfield, SUBFIELDS subfieldSample,
     List<MessageError> errors) {
        int fieldId = parsedField.getId();
        // The right type for the subfield with this id according to MPI.
        String rightType = subfieldSample.getType();
        // The type set for the parsedSubfield.
        String currentType = parsedSubfield.getType();
        // Check of the type value.
        if (currentType.compareTo(rightType) != 0)
            errors.add(new MessageError("The type of the subfield №" + parsedSubfield.getId() +
                    " of the field №" + fieldId + " has incorrect value!"));
        // Check of the type of the content.
        errors = checkTypeOfParsedSubfieldContent(fieldId, parsedSubfield, rightType, errors);
        return errors;
    }

    /*
   Checks the transmitted content of the parsedSubfield on the match with
   the values allowable according to the type of the MIP field.
   */
    private static List<MessageError> checkTypeOfParsedSubfieldContent
    (int fieldId, ParsedSubfield parsedSubfield, String rightType, List<MessageError> errors) {
        int subfieldId = parsedSubfield.getId();
        String content = parsedSubfield.getContent();
        // Binary data represented by hexadecimal numbers.
        if (rightType.compareTo("b") == 0)
            errors = checkCertainTypeOfSubfieldContent(fieldId, subfieldId, content, "[0-9A-F]+",
                    "hexadecimal numbers", errors);
        else {
            // Decimal numbers.
            if (rightType.compareTo("n") == 0)
                errors = checkCertainTypeOfSubfieldContent(fieldId, subfieldId, content, "[0-9]+",
                        "decimal numbers", errors);
                // Decimal numbers, English letters, special symbols.
                // rightType.compareTo("ans") == 0.
            else
                errors = checkCertainTypeOfSubfieldContent(fieldId, subfieldId, content, "[ -~]+",
                        "decimal, hexadecimal numbers, English letters or special symbols", errors);
        }
        return errors;
    }

    /*
    Checks the transmitted content of the parsedSubfield on the match with
    the values allowable according to the type of the MIP field.
    The type is a certain known type.
     */
    private static List<MessageError> checkCertainTypeOfSubfieldContent
    (int fieldId, int subfieldId, String content, String allowableValuesPattern, String allowableValuesWords,
     List<MessageError> errors) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(allowableValuesPattern);
        java.util.regex.Matcher matcher = pattern.matcher(content);
        if (!matcher.matches())
            errors.add(new MessageError("The content \"" + content + "\" of the subfield №" + subfieldId +
                    " of the field №" + fieldId +
                    " has incorrect type of the data!" +
                    " There are only " + allowableValuesWords +" should be."));
        return errors;
    }

    /*
    Checks the length of the transmitted parsedSubfield.
    The lengthMIP and lengthMIP of the content of the field are checking.
    Note, the lengthReal is not considered because this is set during parsing.
    Note, unfixed subfields are not considered!
    (on the strength of teh Lib).
    */
    private static List<MessageError> checkLengthOfParsedSubfield
    (ParsedField parsedField, ParsedSubfield parsedSubfield, List<MessageError> errors) {
        int fieldId = parsedField.getId();
        int subfieldId = parsedSubfield.getId();
        SUBFIELDS subfieldSample = SUBFIELDS.valueOf(fieldId, subfieldId);
        // The right length for the field with this id according to MPI.
        int rightLengthMIP = subfieldSample.getLength();
        // Comparison with the lengthMIP of the parsedSubfield.
        int currentLength = parsedSubfield.getLengthMIP();
        if (currentLength != rightLengthMIP)
            errors.add(new MessageError("The length value of the subfield №" + subfieldId +
                    " of the field  №" + fieldId + " is not correct!"));
        // Check of the lengthMIP of the content of the parsedField.
        int currentContentLengthMIP
                = getContentLengthMIPOfParsedSubfield(parsedSubfield, subfieldSample);
        if (currentContentLengthMIP != rightLengthMIP)
            errors.add(new MessageError("The length of the content of the subfield №" + subfieldId +
                    " of the field №" + fieldId + " is not correct!"));
        return errors;
    }

    /*
    Returns the current length (the real quantity of symbols) of the content of the parsedSubfield.
    */
    private static int getContentLengthMIPOfParsedSubfield
    (ParsedSubfield parsedSubfield, SUBFIELDS subfieldSample) {
        int currentContentLength = parsedSubfield.getContent().length();
        // The first additional zero is considered.
        if (parsedSubfield.getType().compareTo("n") == 0 ||
            parsedSubfield.getType().compareTo("b") == 0) {
            if (subfieldSample.getLength() % 2 != 0)
                currentContentLength -= 1;
        }
        return currentContentLength;
    }

    /*
    Checks the elements of the transmitted parsedField on correctness.
     */
    private static List<MessageError> checkParsedElements
    (ParsedField parsedField, List<MessageError> errors) {
        HashMap<Integer, ParsedElement> elements = parsedField.getElements();
        int fieldId = parsedField.getId();
        for (ParsedElement parsedElement : elements.values()) {
            int elemId = parsedElement.getId();
            ELEMENTS element = ELEMENTS.valueOf(fieldId, elemId);
            // TODO: add the feature below to the documentation.
            if (element == null)
                errors.add(new MessageError("The element №" + elemId + " of the field №" + fieldId +
                        " is not provided by the Lib on the strength of the project" +
                        " or because the Lib does not suggest this!"));
            else {
                errors = checkTypeOfParsedElement(parsedField, parsedElement, errors);
                errors = checkLengthOfParsedElement(parsedField, parsedElement, errors);
            }
        }
        return errors;
    }

    /*
    Checks the type of the transmitted parsedElement.
    The type field and the content field are checking.
     */
    private static List<MessageError> checkTypeOfParsedElement
    (ParsedField parsedField, ParsedElement parsedElement, List<MessageError> errors) {
        int fieldId = parsedField.getId();
        int elemId = parsedElement.getId();
        // Match of the type value of the parsedElement with the type according to MIP.
        String rightType = ELEMENTS.valueOf(fieldId, elemId).getType();
        String currentType = parsedElement.getType();
        // Match of the right type and the current type.
        if (currentType.compareTo(rightType) != 0)
            errors.add(new MessageError("The type of the content of the element №"
                    + elemId + "must be " + rightType + "!"));
        // Match of the type of the right type and the content type.
        errors = checkTypeOfParsedElementContent(elemId, parsedElement, rightType, errors);
        return errors;
    }

    /*
    Checks the transmitted content of the parsedElement on the match with
    the values allowable according to the type of the MIP element.
     */
    private static List<MessageError> checkTypeOfParsedElementContent
    (int id, ParsedElement parsedElement, String rightType, List<MessageError> errors) {
        String content = parsedElement.getContent();
        // Binary data.
        if (rightType.compareTo("%") == 0)
            errors = checkCertainTypeOfElementContent(id, content, "[0-9A-F]+",
                    "hexadecimal numbers", errors);
            // rightType.compareTo("^") == 0.
            // ASCII symbols.
        else
            errors = checkCertainTypeOfElementContent(id, content, "[ -~]+",
                    "ASCII symbols", errors);
        return errors;
    }

    /*
    Checks the transmitted content of the parsedElement on the match with
    the values allowable according to the type of the MIP element.
    The type is a certain known type.
     */
    private static List<MessageError> checkCertainTypeOfElementContent
    (int id, String content, String allowableValuesPattern, String allowableValuesWords,
     List<MessageError> errors) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(allowableValuesPattern);
        java.util.regex.Matcher matcher = pattern.matcher(content);
        if (!matcher.matches())
            errors.add(new MessageError("The content of the element №" + id +
                    " has incorrect type of the data!" +
                    " There are only " + allowableValuesWords + " should be."));
        return errors;
    }

    /*
    Checks the length of the transmitted element of the 48th field.
    Now only the 48th field is considered as a field with elements!
    The length field and the length of the content of the element.
    */
    private static List<MessageError> checkLengthOfParsedElement
    (ParsedField parsedField, ParsedElement parsedElement, List<MessageError> errors) {
        int fieldId = parsedField.getId();
        // The boards of the right length for the field with this elemId according to MPI.
        int elemId = parsedElement.getId();
        ELEMENTS elem = ELEMENTS.valueOf(48, elemId);
        // Now only the 48th field is considered as a field with elements!
        int rightMinLength = elem.getMinLength();
        int rightMaxLength = elem.getMaxLength();
        // The length field set for the parsedField.
        int currentLengthField = getCurrentContentLengthMIPOfParsedElement(parsedElement);
        if (!(rightMinLength <= currentLengthField && currentLengthField <= rightMaxLength))
            errors.add(new MessageError("The length value of the element №" + elemId + " " +
                    "of the field №" + fieldId + " must be correct!"));
        // The length of the content of the parsedElement.
        int currentContentLength = parsedElement.getContent().length();
        if (!(rightMinLength <= currentContentLength && currentContentLength <= rightMaxLength))
            errors.add(new MessageError("The length of the content of the element №" + elemId +
                    " of the field  №" + fieldId + " is not correct!"));
        return errors;
    }

    private static int getCurrentContentLengthMIPOfParsedElement
            (ParsedElement parsedElement) {
        int currentContentLength = parsedElement.getContent().length();
        // The first additional zero is considered.
        if (parsedElement.getType().compareTo("%") == 0) {
            if (parsedElement.getLengthMIP() % 2 != 0)
                currentContentLength -= 1;
        }
        return currentContentLength;
    }

    // Todo: check that it can be removed and remove.
    /*
    Returns true if the error with the transmitted message is contained in the errors list.
    Else returns false.
     */
    private static boolean errorsContainMessageError(String message, List<MessageError> errors) {
        for (MessageError error : errors)
            if (error.getMessage().compareTo(message) == 0)
                return true;
        return false;
    }

    /*
    Accepts a hexSym number which has 1 rank and converts into a binary number with 4 ranks.
    */
    private static String convertHexToBin(char hexSym) {
        switch(hexSym) {
            case '0':
                return "0000";
            case '1':
                return "0001";
            case '2':
                return "0010";
            case '3':
                return "0011";
            case '4':
                return "0100";
            case '5':
                return "0101";
            case '6':
                return "0110";
            case '7':
                return "0111";
            case '8':
                return "1000";
            case '9':
                return "1001";
            case 'A':
                return "1010";
            case 'B':
                return "1011";
            case 'C':
                return "1100";
            case 'D':
                return "1101";
            case 'E':
                return "1110";
            case 'F':
                return "1111";
            default:
                return "";
        }
    }
}

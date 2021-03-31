package com.imohsenb.ISO8583.entities;

import com.imohsenb.ISO8583.enums.ELEMENTS;
import com.imohsenb.ISO8583.enums.FIELDS;
import com.imohsenb.ISO8583.enums.SUBFIELDS;
import com.imohsenb.ISO8583.exceptions.ISOException;
import com.imohsenb.ISO8583.security.ISOMacGenerator;
import com.imohsenb.ISO8583.utils.FixedBitSet;
import com.imohsenb.ISO8583.utils.StringUtil;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * ISO Message Entity
 *
 * @author Mohsen Beiranvand
 */
public class ISOMessage {

    private TreeMap<Integer, byte[]> fields = new TreeMap<>();
    private TreeMap<Integer, Integer> quantitiesOfSignificantSumbols = new TreeMap<>();

    private boolean isNil = true;
    private String message;
    private String mti;
    private byte[] msg;
    private byte[] header;
    private byte[] body;
    private byte[] primaryBitmap;
    private int msgClass;
    private int msgFunction;
    private int msgOrigin;
    private int len = 0;

    public static ISOMessage NullObject() {
        return new ISOMessage();
    }

    public boolean isNil() {
        return isNil;
    }

    public byte[] getHeader() {
        return header;
    }

    public byte[] getBody() {
        return body;
    }

    /**s
     * Get primary bitmap
     *
     * @return returns primary byte array
     * @since 1.0.4-SNAPSHOT
     */
    public byte[] getPrimaryBitmap() {
        return primaryBitmap;
    }

    /**
     * Message length
     *
     * @return returns message length
     */
    public int length() {
        return len;
    }

    /**
     * Get field value in byte array format
     *
     * @param fieldNo field number
     * @return returns field value in byte array format
     * @throws ISOException throws exception
     */
    public byte[] getField(int fieldNo) throws ISOException {
        if (!fields.containsKey(fieldNo))
            throw new ISOException("Field №" + fieldNo + " does not exist!");
        return fields.get(fieldNo);
    }

    /**
     * Get field value in byte array format
     *
     * @param field field in {@link FIELDS} format
     * @return returns field value in byte array format
     */
    public byte[] getField(FIELDS field) {
        return fields.get(field.getNum());
    }

    /**
     * Get field value in string format
     *
     * @param fieldNo field number
     * @return returns field value in String format
     * @throws ISOException throws exception
     */
    public String getStringField(int fieldNo) throws ISOException {
        return getStringField(FIELDS.valueOf(fieldNo));

    }

    /**
     * Get field value in string format
     *
     * @param field field in {@link FIELDS} format
     * @return returns field value in String format
     * @throws ISOException throws exception
     */
    public String getStringField(FIELDS field) throws ISOException {

        return getStringField(field, false);
    }

    /**
     * Get field value in string format
     *
     * @param fieldNo  field number
     * @param asciiFix set true if you want result in ASCII format
     * @return returns field value in String format
     * @throws ISOException throws exception
     */
    public String getStringField(int fieldNo, boolean asciiFix) throws ISOException {
        return getStringField(FIELDS.valueOf(fieldNo), asciiFix);
    }

    /**
     * Get field value in string format
     *
     * @param field    field in {@link FIELDS} format
     * @param asciiFix set true if you want result in ASCII format
     * @return returns field value in String format
     * @throws ISOException throws exception
     */
    public String getStringField(FIELDS field, boolean asciiFix) throws ISOException {
        String temp = StringUtil.fromByteArray(getField(field.getNum()));
        if (asciiFix &&
                field.getType().compareTo("n") != 0 && field.getType().compareTo("b") != 0 &&
                !field.getHasSubfields()            && !field.getHasElements())
            return StringUtil.hexToAscii(temp);
        return temp;
    }

    public TreeMap<Integer, byte[]> getFields() { return fields; }

    /*
    Returns the length of the field with the transmitted id.
    If this field id is not contained in the length, the method returns -1.
     */
    public int getFieldLength(int fieldId) {
        if (!quantitiesOfSignificantSumbols.containsKey(fieldId))
            return -1;
        return quantitiesOfSignificantSumbols.get(fieldId);
    }

    /**
     * Set and parse ISO8583 message from buffer
     *
     * @param message         ISO8583 in byte array format
     * @param headerAvailable set true if header is available in buffer
     * @return returns ISO8583 message in ISOMessage type
     * @throws ISOException throws exception
     */
    public ISOMessage setMessage(byte[] message, boolean headerAvailable) throws ISOException {
        isNil = false;
        msg = message;
        len = msg.length / 2;
        int headerOffset = 0;
        if (headerAvailable) {
            headerOffset = 5;
        }
        try {
            this.header = Arrays.copyOfRange(msg, 0, headerOffset);
            this.body = Arrays.copyOfRange(msg, headerOffset, msg.length);
            this.primaryBitmap = Arrays.copyOfRange(body, 2, 10);

            parseHeader();
            parseBody();

        } catch (Exception e) {
            throw new ISOException(e.getMessage(), e.getCause());
        }
        return this;
    }

    /**
     * Set and parse ISO8583 message from buffer
     *
     * @param message ISO8583 in byte array format
     * @return returns ISO8583 message in ISOMessage type
     * @throws ISOException throws exception
     */
    public ISOMessage setMessage(byte[] message) throws ISOException {
        // false because it is not necessary to use header.
        return this.setMessage(message, false);
    }

    private void parseHeader() {
        if (body.length > 2) {
            mti = StringUtil.fromByteArray(Arrays.copyOfRange(body, 0, 2));
            msgClass = Integer.parseInt(mti.substring(1, 2));
            msgFunction = Integer.parseInt(mti.substring(2, 3));
            msgOrigin = Integer.parseInt(mti.substring(3, 4));
        }
    }

    private void parseBody() throws ISOException {
        FixedBitSet pb = new FixedBitSet(64);
        pb.fromHexString(StringUtil.fromByteArray(primaryBitmap));
        int offset = 10;
        for (int fieldId : pb.getIndexes()) {
            FIELDS field = FIELDS.valueOf(fieldId);
            // If the field is not provided by the Lib, an exception is thrown.
            checkIdOfField(field);
            if (field.isFixed())
                len = getLengthOfFixedField(field);
            else {
                int formatLength = getFormatLength(field);
                int fieldLengthMIP = Integer.valueOf(
                        StringUtil.fromByteArray(Arrays.copyOfRange(body, offset, offset + formatLength)));
                offset = offset + formatLength;
                len = getLengthOfUnfixedField(field, fieldLengthMIP, offset);
                addLength(field, fieldLengthMIP);
                //offset = offset + formatLength;
            }
            // Check that the length of the body is enough.
            try { addElement(field, Arrays.copyOfRange(body, offset, offset + len)); }
            catch (ArrayIndexOutOfBoundsException ex) {
                throw new ArrayIndexOutOfBoundsException("The length of the message is not enough!");
            }
            offset += len;
        }
    }

    private void checkIdOfField(FIELDS field) {
        if (field == null)
            throw new IllegalArgumentException("The information about the field №" + field.getNum() +
                    " is not provided by the Lib on the strength of the project features" +
                    " or because the MIP does not suggest this!");
    }

    private int getLengthOfUnfixedField(FIELDS field, int flen, int offset) throws ISOException {
        int len;
        if (!field.getHasSubfields() && !field.getHasElements())
            len = getCompressedOrUncompressedLengthOfFieldWithoutSubfieldsOrElements(field, flen);
        else {
            if (field.getHasSubfields())
                throw new ISOException("The unfixed field №" + field.getNum() +
                        " with subfields is not provided by the Lib on the strength of the project!");
                // field.getHasElements().
            else
                len = getLengthOfFieldWithElements(field, flen, offset);
        }
        return len;
    }

    private int getFormatLength(FIELDS field) {
        int formatLength = 1;
        switch (field.getFormat()) {
            case "LL":
                formatLength = 1;
                break;
            case "LLL":
                formatLength = 2;
                break;
        }
        return formatLength;
    }

    private int getLengthOfFixedField(FIELDS field) throws ISOException {
        int len;
        if (!field.getHasSubfields() && !field.getHasElements())
            len = getCompressedOrUncompressedLengthOfFieldWithoutSubfieldsOrElements(field, -1);
        else {
            if (field.getHasSubfields())
                len = getLengthOfFieldWithSubfields(field);
            // A fixed field can not have elements according to the MIP.
            else
                throw new ISOException("The fixed field №" + field.getNum() + " must not have elements!");
        }
        return len;
    }

    /*
    Only for fixed subfields!
    */
    private int getCompressedOrUncompressedLengthOfFieldWithoutSubfieldsOrElements(FIELDS field, int flen) {
        int len = 0;
        if (flen != -1)
            len = flen;
        else
            len = field.getMaxLength();
        switch (field.getType()) {
            case "n":
            case "b": {
                if (len % 2 != 0)
                    len++;
                len /= 2;
                break;
            }
            default:
                break;
        }
        return len;
    }

    /*
    Only for fixed fields and fixed subfields!
     */
    private int getLengthOfFieldWithSubfields(FIELDS field) {
        int len = 0;
        for (int subfieldId = 1; subfieldId <= field.getMaxSubfieldsId(); subfieldId++) {
            SUBFIELDS subfield = SUBFIELDS.valueOf(field.getNum(), subfieldId);
            if (subfield == null)
                throw new IllegalArgumentException("The information of the subfield №" + subfieldId +
                        " of the field №" + field.getNum() + " is not provided by the Lib" +
                        " on the strength of the project features or because the MIP does not suggest this!");
            int subfieldLength = subfield.getLength();
            if (subfield.getType().compareTo("n") == 0 || subfield.getType().compareTo("b") == 0)
                subfieldLength = (subfieldLength + 1) / 2;
            len += subfieldLength;
        }
        return len;
    }

    private int getLengthOfFieldWithElements(FIELDS field, int flen, int offset) throws ISOException {
        int fieldId = field.getNum();
        int fieldLengthCurrent = 0;
        //Todo: check that can be removed and remove. line
        //int fieldRealLengthInSymbols = 0;
        int fieldRealLengthBytes = 0;
        // Stop after the last element.
        while (fieldLengthCurrent < flen) {
            if (offset + 5 > body.length)
                throw new ArrayIndexOutOfBoundsException("The field №" + field.getNum() + "has not enough length!");
            int elemId = getIdOfElement(fieldId, offset);
            String elemType = getTypeOfElement(offset);
            int elemLenMIP = getLengthOfElement(offset);
            int elemLengthBytes = getLengthBytesOfElement(fieldId, elemId, elemType, elemLenMIP);
            fieldRealLengthBytes += 1 + 2 + 2 + elemLengthBytes;
            // Todo: check that can be removed and remove.
            //int elemLengthInSymbols = getLengthInSymbolsOfElement(fieldId, elemId, elemType, elemLenMIP);
            //fieldRealLengthInSymbols += elemLengthInSymbols;
            // Todo: the end.
            // type + id + length + content.
            fieldLengthCurrent += 1 + 2 + 2 + elemLenMIP;
        }
        return fieldRealLengthBytes;
    }

    /*
    Returns the id of the field with the transmitted id.
    The element matching to the offset is considered.
     */
    private int getIdOfElement(int fieldId, int offset) throws ISOException {
        String elemIdHex = StringUtil.fromByteArray(Arrays.copyOfRange(body, offset + 1, offset + 3));
        int elemId = Integer.parseInt(StringUtil.hexToAscii(elemIdHex), 16);
        if (ELEMENTS.valueOf(fieldId, elemId) == null)
            throw new ISOException("The information of the №" + elemId +
                    " of the field №" + fieldId + " is not provided by the Lib" +
                    " on the strength of the project features or because the MIP does not suggest this!");
        return elemId;
    }

    private String getTypeOfElement(int offset) {
        String elemTypeHex = StringUtil.fromByteArray(Arrays.copyOfRange(body, offset, offset + 1));
        String elemType = StringUtil.hexToAscii(elemTypeHex);
        return elemType;
    }

    private int getLengthOfElement(int offset) {
        String elemLenHex = StringUtil.fromByteArray(Arrays.copyOfRange(body, offset + 3, offset + 5));
        int elemLenMIP = Integer.parseInt(StringUtil.hexToAscii(elemLenHex), 16);
        return elemLenMIP;
    }

    private int getLengthBytesOfElement(int fieldId, int elemId, String elemType, int elemLenMIP) {
        int elemLengthInBytes = elemLenMIP;
        // Compressed format.
        if (elemType.compareTo("%") == 0) {
            // +1 to consider an odd length.
            elemLengthInBytes = (elemLengthInBytes + 1) / 2;
        }
        // Uncompressed format.
        else {
            if (elemType.compareTo("^") != 0)
                throw new IllegalArgumentException("The type of the element №" + elemId +
                        " of the field №" + fieldId + " is incorrect!");
            // If elemType.compareTo("^") == 0, the elemLengthInBytes has had right value already.
        }
        return elemLengthInBytes;
    }

    private int getLengthInSymbolsOfElement(int fieldId, int elemId, String elemType, int elemLenMIP) {
        int elemLengthInSymbols = elemLenMIP;
        // Compressed format.
        if (elemType.compareTo("%") == 0) {
            if (elemLengthInSymbols % 2 != 0)
                elemLengthInSymbols++; // To consider an odd length.
        }
        // Uncompressed format.
        else {
            if (elemType.compareTo("^") == 0)
                elemLengthInSymbols *= 2;
            else
                throw new IllegalArgumentException("The type of the element №" + elemId +
                        " of the field №" + fieldId + " is incorrect!");
        }
        return elemLengthInSymbols;
    }

    private void addElement(FIELDS field, byte[] data) {
        fields.put(field.getNum(), data);
    }

    private void addLength(FIELDS field, int length) {
        quantitiesOfSignificantSumbols.put(field.getNum(), length);
    }

    /**
     * Get EntrySet
     *
     * @return returns data elements entry set
     */
    public Set<Map.Entry<Integer, byte[]>> getEntrySet() {
        return fields.entrySet();
    }

    /**
     * Check Field exists by {@link FIELDS} enum
     *
     * @param field field enum
     * @return Returns true if field has value in message
     */
    public boolean fieldExits(FIELDS field) {
        return fieldExits(field.getNum());
    }

    /**
     * Check Field exists field number
     *
     * @param no field number
     * @return Returns true if field has value in message
     */
    public boolean fieldExits(int no) {
        return fields.containsKey(no);
    }

    /**
     * Get Message MTI
     * @return returns MTI in String format
     */
    public String getMti() {
        return mti;
    }

    /**
     * Get message class
     * @return returns message class
     */
    public int getMsgClass() {
        return msgClass;
    }

    /**
     * Get message function
     * @return returns message function
     */
    public int getMsgFunction() {
        return msgFunction;
    }

    /**
     * Get message origin
     * @return returns message origin
     */
    public int getMsgOrigin() {
        return msgOrigin;
    }

    /**
     * Validate mac
     * it's useful method to validate response MAC
     *
     * @param isoMacGenerator implementation of {@link ISOMacGenerator}
     * @return returns true if response message MAC is valid
     * @throws ISOException throws exception
     */
    public boolean validateMac(ISOMacGenerator isoMacGenerator) throws ISOException {

        if (!fieldExits(FIELDS.F64_MAC) || getField(FIELDS.F64_MAC).length == 0) {
            System.out.println("validate mac : not exists");
            return false;
        }
        byte[] mBody = new byte[getBody().length - 8];
        System.arraycopy(getBody(), 0, mBody, 0, getBody().length - 8);
        byte[] oMac = Arrays.copyOf(getField(FIELDS.F64_MAC), 8);
        byte[] vMac = isoMacGenerator.generate(mBody);

        return Arrays.equals(oMac, vMac);
    }

    /**
     * Convert ISOMessage to String
     * @return ISOMessage in String format
     */
    public String toString() {
        if (message == null)
            message = StringUtil.fromByteArray(msg);
        return message;
    }

    /**
     * Convert all fields in String format
     * @return returns strings of fields
     */
    public String fieldsToString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("\r\n");
        for (Map.Entry<Integer, byte[]> item :
                fields.entrySet()) {
            stringBuilder
                    .append(FIELDS.valueOf(item.getKey()).name())
                    .append(" : ")
                    .append(StringUtil.fromByteArray(item.getValue()))
                    .append("\r\n");
        }
        stringBuilder.append("\r\n");
        return stringBuilder.toString();
    }

    /**
     * Clean up message
     */
    public void clear() {

        Arrays.fill(header, (byte) 0);
        Arrays.fill(body, (byte) 0);
        Arrays.fill(primaryBitmap, (byte) 0);

        message = null;
        header = null;
        body = null;
        primaryBitmap = null;

    }

}
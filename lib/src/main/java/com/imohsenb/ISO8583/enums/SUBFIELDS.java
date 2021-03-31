package com.imohsenb.ISO8583.enums;

import java.util.HashMap;
import java.util.Map;

public enum SUBFIELDS {
    //                                      |fieldNum   |subFieldNum    |type   |length       |fixed     |format    |beginInd
    F1_OperationType                        ( 3,1, "n",  2, true, null,  0),
    F2_SenderAccountType                    ( 3,2, "n",  2, true, null,  2),
    F3_BeneficiaryAccountType               ( 3,3, "n",  2, true, null,  4),

    F1_POSTerminalPANEntryMode              (22,1, "n",  2, true, null,  0),
    F1_POSTerminalPINEntryCapability        (22,2, "n",  1, true, null,  2),
    // Required for the project.
    F1_NetworkIdentifier                    (63,1, "an",  4, true, null,  0),
    F2_TransactionReferenceNumber           (63,2, "n",  16, true, null,  8),
    F3_CardProductID                        (63,3, "ans", 3, true, null, 24);
    // Required for the project. The end.

    private final int fieldNum;
    private final int subFieldNum;
    private final String type;
    private final int length;
    private final boolean fixed;
    private final String format;
    // According to the real position in the field.
    // Can be different from the MIP value.
    private final int beginInd;

    SUBFIELDS(int fieldNum, int subFieldNum, String type, int length, boolean fixed, String format, int beginInd) {
        this.fieldNum = fieldNum;
        this.subFieldNum = subFieldNum;
        this.type = type;
        this.length = length;
        this.fixed = fixed;
        this.format = format;
        this.beginInd = beginInd;
    }

    private static Map<KeyForFieldComponent, SUBFIELDS> map = new HashMap<KeyForFieldComponent, SUBFIELDS>();

    static {
        for (SUBFIELDS _subfields : SUBFIELDS.values()) {
            map.put(_subfields.getKeyForSubfield(), _subfields);
        }
    }

    private KeyForFieldComponent getKeyForSubfield() {
        return new KeyForFieldComponent(fieldNum, subFieldNum);
    }

    public String getType() {
        return type;
    }

    public int getLength() {
        return length;
    }

    public boolean isFixed() {
        return fixed;
    }

    public String getFormat() {
        return format;
    }

    public int getBeginInd() { return this.beginInd;}

    public static SUBFIELDS valueOf(int fieldNum, int subFieldNum) {
        KeyForFieldComponent key = new KeyForFieldComponent(fieldNum, subFieldNum);
        for (KeyForFieldComponent mapKey : map.keySet()) {
            // If this key is included in the map.
            if (mapKey.getFieldId() == key.getFieldId() && mapKey.getComponentId() == key.getComponentId())
                return map.get(mapKey);
        }
        // If this key is not included in the map.
        return null;
    }
}

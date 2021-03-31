package com.imohsenb.ISO8583.enums;

import java.util.HashMap;
import java.util.Map;

public enum ELEMENTS {
    //                          |fieldNum   |elemNum      |type    |minLen       |maxLen           |fixed          |format
    E97_TextMessageToRecipient (48, 97,  "%",   1, 200,   false,  "LLL");

    private final int fieldNum;
    private final int elemNum;
    private final String type;
    private final int minLength;
    private final int maxLength;
    private final boolean fixed;
    private final String format;

    ELEMENTS(int fieldNum, int elemNum, String type,
             int minLength, int maxLength,
             boolean fixed, String format) {
        this.fieldNum = fieldNum;
        this.elemNum = elemNum;
        this.type = type;
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.fixed = fixed;
        this.format = format;
    }

    private static Map<KeyForFieldComponent, ELEMENTS> map = new HashMap<KeyForFieldComponent, ELEMENTS>();

    static {
        for (ELEMENTS _elements : ELEMENTS.values()) {
            map.put(_elements.getKeyForElement(), _elements);
        }
    }

    private KeyForFieldComponent getKeyForElement() {
        return new KeyForFieldComponent(fieldNum, elemNum);
    }

    public String getType() {
        return type;
    }

    public int getMinLength() { return minLength; }

    public int getMaxLength() {
        return maxLength;
    }

    public boolean isFixed() {
        return fixed;
    }

    public String getFormat() {
        return format;
    }

    public static ELEMENTS valueOf(int fieldNum, int elemNum) {
        KeyForFieldComponent key = new KeyForFieldComponent(fieldNum, elemNum);
        for (KeyForFieldComponent mapKey : map.keySet()) {
            // If this key is included in the map.
            if (mapKey.getFieldId() == key.getFieldId() && mapKey.getComponentId() == key.getComponentId())
                return map.get(mapKey);
        }
        // If this key is not included in the map.
        return null;
    }
}

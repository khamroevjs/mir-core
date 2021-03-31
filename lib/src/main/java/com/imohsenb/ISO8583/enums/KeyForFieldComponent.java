package com.imohsenb.ISO8583.enums;

public class KeyForFieldComponent {

    private int fieldId;
    private int componentId;

    public int getFieldId() {
        return fieldId;
    }

    public int getComponentId() { return componentId; }

    public KeyForFieldComponent(int fieldId, int componentId) {
        this.fieldId = fieldId;
        this.componentId = componentId;
    }
}


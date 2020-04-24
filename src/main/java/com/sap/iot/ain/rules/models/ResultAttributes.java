package com.sap.iot.ain.rules.models;


import java.util.List;

public class ResultAttributes {
    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    public String getKeyFieldValue() {
        return keyFieldValue;
    }

    public void setKeyFieldValue(String keyFieldValue) {
        this.keyFieldValue = keyFieldValue;
    }

    private List<Attribute> attributes;
    private String keyFieldValue;

    
    
}

package com.sap.iot.ain.rules.models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.sap.iot.ain.reuse.TimestampSerializer;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AttributeValue implements RowMapper<AttributeValue> {

    private String client;
    private String modelId;
    private String attributeId;
    private String booleanValue;
    @JsonSerialize(using = TimestampSerializer.class)
    private Timestamp dateValue;
    private String stringValue;
    private String numericValue;

    private static final Logger logger = LoggerFactory.getLogger(AttributeValue.class);

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(String attributeId) {
        this.attributeId = attributeId;
    }

    public String getBooleanValue() {
        return booleanValue;
    }

    public void setBooleanValue(String booleanValue) {
        this.booleanValue = booleanValue;
    }

    public Timestamp getDateValue() {
        return dateValue;
    }

    public void setDateValue(Timestamp dateValue) {
        this.dateValue = dateValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public String getNumericValue() {
        return numericValue;
    }

    public void setNumericValue(String numericValue) {
        this.numericValue = numericValue;
    }

    @Override
    public AttributeValue mapRow(ResultSet rs, int rowNum) {
        AttributeValue value = null;

        try {
            value = this.getClass().newInstance();
            value.setClient(rs.getString("Client"));
            value.setAttributeId(rs.getString("PropertyID"));
            value.setModelId(rs.getString("ID"));
            value.setBooleanValue(rs.getString("BooleanValue"));
            value.setDateValue(rs.getTimestamp("DateValue"));
            value.setStringValue(rs.getString("StringValue"));
            value.setNumericValue(rs.getString("Norm_1"));
        } catch (IllegalAccessException | InstantiationException | SQLException e) {
            logger.error("Exception in AttributeValue.mapRow");
        }
        return value;
    }

}

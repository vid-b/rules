package com.sap.iot.ain.rules.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "\"sap.ain.metaData::Rules.RuleAggregation\"")
@IdClass(RuleAggregationPK.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RuleAggregation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "\"Client\"", nullable = true, length = 32)
    private String client;

    @Id
    @Column(name = "\"ID\"", nullable = true, length = 32)
    private String id;

    @Column(name = "\"RuleID\"", length = 32)
    private String ruleID;

    @Column(name = "\"SequenceNumber\"")
    private int sequenceNumber;

    @Column(name = "\"Name\"", length = 200)
    private String name;

    @Column(name = "\"FieldID\"", length = 200)
    private String fieldID;

    @Column(name = "\"FieldIDType\"", length = 200)
    private String fieldIDType;

    @Column(name = "\"FieldExt\"", length = 200)
    private String fieldProperty;

    @Column(name = "\"FieldPST\"", length = 200)
    private String fieldGroup;

    @Column(name = "\"FieldTemplate\"", length = 200)
    private String fieldTemplate;

    @Column(name = "\"FieldTemplateType\"", length = 200)
    private String fieldTemplateType;

    @Column(name = "\"FunctionName\"", length = 200)
    private String functionName;

    @Column(name = "\"TimeFilter\"", length = 200)
    private String timeFilter;

    @Column(name = "\"TimeUnit\"", length = 200)
    private String timeUnit;

    @Column(name = "\"TimeField\"", length = 200)
    private String timeField;

    @Column(name = "\"GroupByFields\"", length = 500)
    private String groupByFields;

    @Transient
    private String fieldPST;

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRuleID() {
        return ruleID;
    }

    public void setRuleID(String ruleID) {
        this.ruleID = ruleID;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFieldID() {
        return fieldID;
    }

    public void setFieldID(String fieldID) {
        this.fieldID = fieldID;
    }

    public String getFieldIDType() {
        return fieldIDType;
    }

    public void setFieldIDType(String fieldIDType) {
        this.fieldIDType = fieldIDType;
    }

    public String getFieldProperty() {
        return fieldProperty;
    }

    public void setFieldProperty(String fieldProperty) {
        this.fieldProperty = fieldProperty;
    }

    public String getFieldGroup() {
        return fieldGroup;
    }

    public void setFieldGroup(String fieldGroup) {
        this.fieldGroup = fieldGroup;
    }

    public String getFieldTemplate() {
        return fieldTemplate;
    }

    public void setFieldTemplate(String fieldTemplate) {
        this.fieldTemplate = fieldTemplate;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public String getTimeFilter() {
        return timeFilter;
    }

    public void setTimeFilter(String timeFilter) {
        this.timeFilter = timeFilter;
    }

    public String getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(String timeUnit) {
        this.timeUnit = timeUnit;
    }

    public String getTimeField() {
        return timeField;
    }

    public void setTimeField(String timeField) {
        this.timeField = timeField;
    }

    public String getGroupByFields() {
        return groupByFields;
    }

    public void setGroupByFields(String groupByFields) {
        this.groupByFields = groupByFields;
    }

    public String getFieldPST() {
        return fieldPST;
    }

    public void setFieldPST(String fieldPST) {
        this.fieldPST = fieldPST;
    }

    public String getFieldTemplateType() {
        return fieldTemplateType;
    }

    public void setFieldTemplateType(String fieldTemplateType) {
        this.fieldTemplateType = fieldTemplateType;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + Objects.hashCode(this.client);
        hash = 53 * hash + Objects.hashCode(this.id);
        hash = 53 * hash + Objects.hashCode(this.ruleID);
        hash = 53 * hash + this.sequenceNumber;
        hash = 53 * hash + Objects.hashCode(this.name);
        hash = 53 * hash + Objects.hashCode(this.fieldID);
        hash = 53 * hash + Objects.hashCode(this.fieldIDType);
        hash = 53 * hash + Objects.hashCode(this.fieldProperty);
        hash = 53 * hash + Objects.hashCode(this.fieldGroup);
        hash = 53 * hash + Objects.hashCode(this.fieldTemplate);
        hash = 53 * hash + Objects.hashCode(this.fieldTemplateType);
        hash = 53 * hash + Objects.hashCode(this.functionName);
        hash = 53 * hash + Objects.hashCode(this.timeFilter);
        hash = 53 * hash + Objects.hashCode(this.timeUnit);
        hash = 53 * hash + Objects.hashCode(this.timeField);
        hash = 53 * hash + Objects.hashCode(this.groupByFields);
        hash = 53 * hash + Objects.hashCode(this.fieldPST);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RuleAggregation other = (RuleAggregation) obj;
        if (this.sequenceNumber != other.sequenceNumber) {
            return false;
        }
        if (!Objects.equals(this.client, other.client)) {
            return false;
        }
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.ruleID, other.ruleID)) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.fieldID, other.fieldID)) {
            return false;
        }
        if (!Objects.equals(this.fieldIDType, other.fieldIDType)) {
            return false;
        }
        if (!Objects.equals(this.fieldProperty, other.fieldProperty)) {
            return false;
        }
        if (!Objects.equals(this.fieldGroup, other.fieldGroup)) {
            return false;
        }
        if (!Objects.equals(this.fieldTemplate, other.fieldTemplate)) {
            return false;
        }
        if (!Objects.equals(this.fieldTemplateType, other.fieldTemplateType)) {
            return false;
        }
        if (!Objects.equals(this.functionName, other.functionName)) {
            return false;
        }
        if (!Objects.equals(this.timeFilter, other.timeFilter)) {
            return false;
        }
        if (!Objects.equals(this.timeUnit, other.timeUnit)) {
            return false;
        }
        if (!Objects.equals(this.timeField, other.timeField)) {
            return false;
        }
        if (!Objects.equals(this.groupByFields, other.groupByFields)) {
            return false;
        }
        if (!Objects.equals(this.fieldPST, other.fieldPST)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "RuleAggregationNew{" + "client=" + client + ", id=" + id + ", ruleID=" + ruleID + ", sequenceNumber=" + sequenceNumber + ", name=" + name + ", fieldID=" + fieldID + ", fieldIDType=" + fieldIDType + ", fieldProperty=" + fieldProperty + ", fieldGroup=" + fieldGroup + ", fieldTemplate=" + fieldTemplate + ", fieldTemplateType=" + fieldTemplateType + ", functionName=" + functionName + ", timeFilter=" + timeFilter + ", timeUnit=" + timeUnit + ", timeField=" + timeField + ", groupByFields=" + groupByFields + ", fieldPST=" + fieldPST + '}';
    }
    
}

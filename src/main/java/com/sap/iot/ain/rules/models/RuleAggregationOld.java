package com.sap.iot.ain.rules.models;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Entity
@Table(name = "\"sap.ain.metaData::Rules.RuleAggregation\"")
@IdClass(RuleAggregationPK.class)
public class RuleAggregationOld implements Serializable {

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
    private String fieldExt;

    @Column(name = "\"FieldPST\"", length = 200)
    private String fieldPst;

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

    public String getFieldExt() {
        return fieldExt;
    }

    public void setFieldExt(String fieldExt) {
        this.fieldExt = fieldExt;
    }

    public String getFieldPst() {
        return fieldPst;
    }

    public void setFieldPst(String fieldPst) {
        this.fieldPst = fieldPst;
    }

    public String getFieldTemplate() {
        return fieldTemplate;
    }

    public void setFieldTemplate(String fieldTemplate) {
        this.fieldTemplate = fieldTemplate;
    }

    public String getFieldTemplateType() {
        return fieldTemplateType;
    }

    public void setFieldTemplateType(String fieldTemplateType) {
        this.fieldTemplateType = fieldTemplateType;
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

    @Override
    public String toString() {
        return "RuleAggregation [id=" + id + ", ruleID=" + ruleID + ", sequenceNumber=" + sequenceNumber + ", name="
                + name + ", fieldID=" + fieldID + ", fieldIDType=" + fieldIDType + ", fieldExt=" + fieldExt
                + ", fieldPst=" + fieldPst + ", fieldTemplate=" + fieldTemplate + ", fieldTemplateType="
                + fieldTemplateType + ", functionName=" + functionName + ", timeFilter=" + timeFilter + ", timeUnit="
                + timeUnit + ", timeField=" + timeField + ", groupByFields=" + groupByFields + "]";
    }
}

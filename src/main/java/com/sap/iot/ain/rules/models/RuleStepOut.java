package com.sap.iot.ain.rules.models;

public class RuleStepOut {
    public int getSequence() {
		return sequence;
	}
	public void setSequence(int sequence) {
		this.sequence = sequence;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public String getOperator() {
		return operator;
	}
	public void setOperator(String operator) {
		this.operator = operator;
	}
	public String getCondition() {
		return condition;
	}
	public void setCondition(String condition) {
		this.condition = condition;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getField2Type() {
		return field2type;
	}
	public void setField2Type(String type) {
		this.field2type = type;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public int getIsStepOperator() {
		return isStepOperator;
	}
	public void setIsStepOperator(int isStepOperator) {
		this.isStepOperator = isStepOperator;
	}
	public int sequence;
    public int level;
    public String operator;
    public String condition;
    public String type;
    public String field2type;
    public String value;
	public int isStepOperator;
}

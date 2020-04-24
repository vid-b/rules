package com.sap.iot.ain.rules.models;

public class EmailData {
	
	private String modelId;
	private String equipmentId;
	private String alertCreatedOn;
	private String alertType;
	private String alertSeverity;
	private String alertProperty;
	private String alertId;
	private String subDomain;
	public String getModelId() {
		return modelId;
	}
	public void setModelId(String modelId) {
		this.modelId = modelId;
	}
	public String getEquipmentId() {
		return equipmentId;
	}
	public void setEquipmentId(String equipmentId) {
		this.equipmentId = equipmentId;
	}
	public String getAlertCreatedOn() {
		return alertCreatedOn;
	}
	public void setAlertCreatedOn(String alertCreatedOn) {
		this.alertCreatedOn = alertCreatedOn;
	}
	public String getAlertType() {
		return alertType;
	}
	public void setAlertType(String alertType) {
		this.alertType = alertType;
	}
	
	public String getAlertSeverity() {
		return alertSeverity;
	}
	public void setAlertSeverity(String alertSeverity) {
		this.alertSeverity = alertSeverity;
	}
	public String getAlertProperty() {
		return alertProperty;
	}
	public void setAlertProperty(String alertProperty) {
		this.alertProperty = alertProperty;
	}
	public String getAlertId() {
		return alertId;
	}
	public void setAlertId(String alertId) {
		this.alertId = alertId;
	}
	public String getSubDomain() {
		return subDomain;
	}
	public void setSubDomain(String subDomain) {
		this.subDomain = subDomain;
	}
}

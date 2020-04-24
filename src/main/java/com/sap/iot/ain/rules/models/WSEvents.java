
package com.sap.iot.ain.rules.models;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"correlationId", "businessTimeStamp", "status", "type", "eventType",
		"description", "severity", "source", "code", "thingId", "thingProperty", "externalId", "alert_details"})
public class WSEvents {

	@JsonProperty("correlationId")
	private String correlationId;
	@JsonProperty("businessTimeStamp")
	private String businessTimeStamp;
	@JsonProperty("status")
	private String status;
	@JsonProperty("type")
	private String type;
	@JsonProperty("eventType")
	private String eventType;
	@JsonProperty("description")
	private String description;
	@JsonProperty("severity")
	private Integer severity;
	@JsonProperty("source")
	private String source;
	@JsonProperty("code")
	private String code;
	@JsonProperty("thingId")
	private String thingId;
	@JsonProperty("thingProperty")
	private String thingProperty;
	@JsonProperty("externalId")
	private String externalId;
	@JsonProperty("alert_details")
	private Map<String, Object> alert_details;

	@JsonProperty("eventType")
	public String getEventType() {
		return eventType;
	}

	@JsonProperty("eventType")
	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	@JsonProperty("businessTimeStamp")
	public String getBusinessTimeStamp() {
		return businessTimeStamp;
	}

	@JsonProperty("businessTimeStamp")
	public void setBusinessTimeStamp(String businessTimeStamp) {
		this.businessTimeStamp = businessTimeStamp;
	}

	@JsonProperty("status")
	public String getStatus() {
		return status;
	}

	@JsonProperty("status")
	public void setStatus(String status) {
		this.status = status;
	}

	@JsonProperty("type")
	public String getType() {
		return type;
	}

	@JsonProperty("type")
	public void setType(String type) {
		this.type = type;
	}

	@JsonProperty("severity")
	public Integer getSeverity() {
		return severity;
	}

	@JsonProperty("severity")
	public void setSeverity(Integer severity) {
		this.severity = severity;
	}

	@JsonProperty("source")
	public String getSource() {
		return source;
	}

	@JsonProperty("source")
	public void setSource(String source) {
		this.source = source;
	}

	@JsonProperty("code")
	public String getCode() {
		return code;
	}

	@JsonProperty("code")
	public void setCode(String code) {
		this.code = code;
	}

	@JsonProperty("thingId")
	public String getThingId() {
		return thingId;
	}

	@JsonProperty("thingId")
	public void setThingId(String thingId) {
		this.thingId = thingId;
	}

	@JsonProperty("thingProperty")
	public String getThingProperty() {
		return thingProperty;
	}

	@JsonProperty("thingProperty")
	public void setThingProperty(String thingProperty) {
		this.thingProperty = thingProperty;
	}

	@JsonProperty("correlationId")
	public String getCorrelationId() {
		return correlationId;
	}

	@JsonProperty("correlationId")
	public void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
	}

	@JsonProperty("description")
	public String getDescription() {
		return description;
	}

	@JsonProperty("description")
	public void setDescription(String description) {
		this.description = description;
	}

	@JsonProperty("externalId")
	public String getExternalId() {
		return externalId;
	}

	@JsonProperty("externalId")
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	@JsonProperty("alert_details")
	public Map<String, Object> getAlert_details() {
		return alert_details;
	}

	@JsonProperty("alert_details")
	public void setAlert_details(Map<String, Object> alert_details) {
		this.alert_details = alert_details;
	}


}

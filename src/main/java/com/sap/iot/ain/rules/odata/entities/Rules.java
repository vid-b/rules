package com.sap.iot.ain.rules.odata.entities;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Id;

import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;

import com.sap.iot.ain.odata.core.ODataEntity;
import com.sap.iot.ain.odata.core.annotations.Entity;
import com.sap.iot.ain.odata.core.annotations.FacetsProperty;
import com.sap.iot.ain.odata.core.annotations.SystemParameters;
import com.sap.iot.ain.odata.core.annotations.Table;
import com.sap.iot.ain.security.Secure;

@Table(name = Rules.DB_TABLE)
@Entity(name = "Rules")
@SystemParameters(client = true, language = true, scope = true, user_bp_id = true)
@Secure(read = {"EQUIPMENT_READ"}, write = {"EQIUPMENT_EDIT"}, delete = {"EQUIPMENT_DELETE"})
public class Rules extends ODataEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String DB_TABLE = "\"_SYS_BIC\".\"sap.ain.views.rule/Rules\"";

	@Id
	@Column(name = "\"RuleID\"")
	@FacetsProperty(maxLength = 200, nullable = false)
	public String ruleId;

	@Column(name = "\"RuleName\"")
	@FacetsProperty(maxLength = 255)
	public String ruleName;

	@Column(name = "\"RuleDescription\"")
	@FacetsProperty(maxLength = 200)
	public String ruleDescription;

	@Column(name = "\"ModelID\"")
	@FacetsProperty(maxLength = 200)
	public String modelId;

	@Id
	@Column(name = "\"EquipmentID\"")
	@FacetsProperty(maxLength = 200)
	public String equipmentID;

	@Column(name = "\"ModelDescription\"")
	@FacetsProperty(maxLength = 200)
	public String modelDescription;

	@Column(name = "\"EquipmentDescription\"")
	@FacetsProperty(maxLength = 200)
	public String equipmentDescription;

	@Column(name = "\"SubjectType\"")
	@FacetsProperty(maxLength = 32)
	public Integer subjectType;

	@Column(name = "\"IsEnabled\"")
	@FacetsProperty(maxLength = 10)
	public String isEnabled;

	@Column(name = "\"AlertTypeExternalID\"")
	@FacetsProperty(maxLength = 256)
	public String alertTypeExternalID;

	@Column(name = "\"LastExecutionTime\"")
	@FacetsProperty(edmSimpleTypeKind = EdmSimpleTypeKind.DateTime)
	public Timestamp lastExecutionTime;
	
	@Column(name = "\"LastExecutionStatus\"")
	public String lastExecutionStatus;
	
	@Column(name = "\"LastSuccessfullExecutionTime\"")
	@FacetsProperty(edmSimpleTypeKind = EdmSimpleTypeKind.DateTime)
	public Timestamp lastSuccessfullExecutionTime;
	
	@Column(name = "\"ActivationStatus\"")
	@FacetsProperty(maxLength = 40)
	public String activationStatus;
	
	@Column(name = "\"EventID\"")
	@FacetsProperty(maxLength = 32)
	public String eventID;
	
	@Column(name = "\"AlertTypeID\"")
	@FacetsProperty(maxLength = 32)
	public String alertTypeID;

	@Column(name="\"AlertTypeDescription\"")
	@FacetsProperty(maxLength = 255)
	public String alertTypeDescription;
	
	@Column(name="\"Source\"")
	@FacetsProperty(maxLength = 255)
	public String source;
	
	
	public String getRuleId() {
		return ruleId;
	}

	public void setRuleId(String ruleId) {
		this.ruleId = ruleId;
	}

	public String getRuleName() {
		return ruleName;
	}

	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}

	public String getRuleDescription() {
		return ruleDescription;
	}

	public void setRuleDescription(String ruleDescription) {
		this.ruleDescription = ruleDescription;
	}


	public String getModelId() {
		return modelId;
	}

	public void setModelId(String modelId) {
		this.modelId = modelId;
	}

	public String getEquipmentID() {
		return equipmentID;
	}

	public void setEquipmentID(String equipmentID) {
		this.equipmentID = equipmentID;
	}

	public String getModelDescription() {
		return modelDescription;
	}

	public void setModelDescription(String modelDescription) {
		this.modelDescription = modelDescription;
	}

	public String getEquipmentDescription() {
		return equipmentDescription;
	}

	public void setEquipmentDescription(String equipmentDescription) {
		this.equipmentDescription = equipmentDescription;
	}

	public Integer getSubjectType() {
		return subjectType;
	}

	public void setSubjectType(Integer subjectType) {
		this.subjectType = subjectType;
	}

	public String getIsEnabled() {
		return isEnabled;
	}

	public void setIsEnabled(String isEnabled) {
		this.isEnabled = isEnabled;
	}


	public String getAlertTypeExternalID() {
		return alertTypeExternalID;
	}

	public void setAlertTypeExternalID(String alertTypeExternalID) {
		this.alertTypeExternalID = alertTypeExternalID;
	}

	public Timestamp getLastExecutionTime() {
		return lastExecutionTime;
	}

	public void setLastExecutionTime(Timestamp lastExecutionTime) {
		this.lastExecutionTime = lastExecutionTime;
	}

	public String getLastExecutionStatus() {
		return lastExecutionStatus;
	}

	public void setLastExecutionStatus(String lastExecutionStatus) {
		this.lastExecutionStatus = lastExecutionStatus;
	}

	public Timestamp getLastSuccessfullExecutionTime() {
		return lastSuccessfullExecutionTime;
	}

	public void setLastSuccessfullExecutionTime(Timestamp lastSuccessfullExecutionTime) {
		this.lastSuccessfullExecutionTime = lastSuccessfullExecutionTime;
	}

	public String getActivationStatus() {
		return activationStatus;
	}

	public void setActivationStatus(String activationStatus) {
		this.activationStatus = activationStatus;
	}

	public String getEventID() {
		return eventID;
	}

	public void setEventID(String eventID) {
		this.eventID = eventID;
	}
	public String getAlertTypeID() {
		return alertTypeID;
	}

	public void setAlertTypeID(String alertTypeID) {
		this.alertTypeID = alertTypeID;
	}
	
	/**
	 * @return the alertTypeDescription
	 */
	public String getAlertTypeDescription() {
		return alertTypeDescription;
	}

	/**
	 * @param alertTypeDescription the alertTypeDescription to set
	 */
	public void setAlertTypeDescription(String alertTypeDescription) {
		this.alertTypeDescription = alertTypeDescription;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
}

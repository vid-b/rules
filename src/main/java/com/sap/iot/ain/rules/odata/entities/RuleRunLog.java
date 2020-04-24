package com.sap.iot.ain.rules.odata.entities;

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

@Table(name = RuleRunLog.DB_TABLE)
@Entity(name = "RuleExecutionLogs")
@SystemParameters(client = true)
@Secure(read = {"EQUIPMENT_READ"}, write = {"EQUIPMENT_EDIT"}, delete = {"EQUIPMENT_DELETE"})
public class RuleRunLog extends ODataEntity {

	public static final String DB_TABLE = "\"_SYS_BIC\".\"sap.ain.views.rule/RuleExecutionLogs\"";

	@Column(name = "\"RuleID\"")
	@FacetsProperty(maxLength = 32)
	private String ruleID;

	@Id
	@Column(name = "\"RuleRunID\"")
	@FacetsProperty(maxLength = 32)
	private String ruleRunID;

	@Column(name = "\"RuleRunTimestamp\"")
	@FacetsProperty(edmSimpleTypeKind = EdmSimpleTypeKind.DateTime)
	private Timestamp ruleRunTimestamp;

	@Column(name = "\"ExecutionStatus\"")
	@FacetsProperty(maxLength = 32)
	private String executionStatus;

	@Column(name = "\"Message\"")
	@FacetsProperty(maxLength = 4096)
	private String message;

	public String getRuleID() {
		return ruleID;
	}

	public void setRuleID(String ruleID) {
		this.ruleID = ruleID;
	}

	public String getRuleRunID() {
		return ruleRunID;
	}

	public void setRuleRunID(String ruleRunID) {
		this.ruleRunID = ruleRunID;
	}

	public Timestamp getRuleRunTimestamp() {
		return ruleRunTimestamp;
	}

	public void setRuleRunTimestamp(Timestamp ruleRunTimestamp) {
		this.ruleRunTimestamp = ruleRunTimestamp;
	}

	public String getExecutionStatus() {
		return executionStatus;
	}

	public void setExecutionStatus(String executionStatus) {
		this.executionStatus = executionStatus;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}

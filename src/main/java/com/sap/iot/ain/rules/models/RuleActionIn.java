package com.sap.iot.ain.rules.models;

import java.util.List;


public class RuleActionIn {
	public String getActionType() {
		return actionType;
	}
	public void setActionType(String actionType) {
		this.actionType = actionType;
	}
	public List<RuleRecipients> getRecipients() {
		return recipients;
	}
	public void setRecipients(List<RuleRecipients> recipients) {
		this.recipients = recipients;
	}

	public String actionType;
	public List<RuleRecipients> recipients;
	private String actionID;


	public String getActionID() {
		return actionID;
	}

	public void setActionID(String actionID) {
		this.actionID = actionID;
	}
}

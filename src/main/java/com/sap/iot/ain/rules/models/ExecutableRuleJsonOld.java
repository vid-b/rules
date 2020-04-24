/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package com.sap.iot.ain.rules.models;

import java.util.List;
import java.util.Objects;

public class ExecutableRuleJsonOld {

	private String client;

	private String ruleId;

	private String name;

	private String description;

	private String priority;

	private String isEnabled;

	private RuleSubjectOld ruleSubject;
	
	private List<RuleAggregationOld> ruleAggregations;

	private List<RuleStepOld> ruleSteps;

	private List<RuleEvent> ruleEvents;

	private List<RuleAction> ruleActions;
	
	private String tenantId;

	public String getRuleId() {
		return ruleId;
	}

	public void setRuleId(String ruleId) {
		this.ruleId = ruleId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public String getIsEnabled() {
		return isEnabled;
	}

	public void setIsEnabled(String isEnabled) {
		this.isEnabled = isEnabled;
	}

	public RuleSubjectOld getRuleSubject() {
		return ruleSubject;
	}

	public void setRuleSubject(RuleSubjectOld ruleSubjects) {
		this.ruleSubject = ruleSubjects;
	}

	public List<RuleAggregationOld> getRuleAggregations() {
		return ruleAggregations;
	}

	public void setRuleAggregations(List<RuleAggregationOld> ruleAggregations) {
		this.ruleAggregations = ruleAggregations;
	}

	public List<RuleStepOld> getRuleSteps() {
		return ruleSteps;
	}

	public void setRuleSteps(List<RuleStepOld> ruleSteps) {
		this.ruleSteps = ruleSteps;
	}

	public List<RuleEvent> getRuleEvents() {
		return ruleEvents;
	}

	public void setRuleEvents(List<RuleEvent> ruleEvents) {
		this.ruleEvents = ruleEvents;
	}

	public List<RuleAction> getRuleActions() {
		return ruleActions;
	}

	public void setRuleActions(List<RuleAction> ruleActions) {
		this.ruleActions = ruleActions;
	}

	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}
	
	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 59 * hash + Objects.hashCode(this.ruleId);
		hash = 59 * hash + Objects.hashCode(this.name);
		hash = 59 * hash + Objects.hashCode(this.description);
		hash = 59 * hash + Objects.hashCode(this.priority);
		hash = 59 * hash + Objects.hashCode(this.isEnabled);
		hash = 59 * hash + Objects.hashCode(this.ruleSubject);
		hash = 59 * hash + Objects.hashCode(this.ruleAggregations);
		hash = 59 * hash + Objects.hashCode(this.ruleSteps);
		hash = 59 * hash + Objects.hashCode(this.ruleEvents);
		hash = 59 * hash + Objects.hashCode(this.ruleActions);
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
		final ExecutableRuleJsonOld other = (ExecutableRuleJsonOld) obj;
		if (!Objects.equals(this.ruleId, other.ruleId)) {
			return false;
		}
		if (!Objects.equals(this.name, other.name)) {
			return false;
		}
		if (!Objects.equals(this.description, other.description)) {
			return false;
		}
		if (!Objects.equals(this.priority, other.priority)) {
			return false;
		}
		if (!Objects.equals(this.isEnabled, other.isEnabled)) {
			return false;
		}
		if (!Objects.equals(this.ruleSubject, other.ruleSubject)) {
			return false;
		}
		if (!Objects.equals(this.ruleAggregations, other.ruleAggregations)) {
			return false;
		}
		if (!Objects.equals(this.ruleSteps, other.ruleSteps)) {
			return false;
		}
		if (!Objects.equals(this.ruleEvents, other.ruleEvents)) {
			return false;
		}
		if (!Objects.equals(this.ruleActions, other.ruleActions)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "ExecutableRuleJson{" + "ruleId=" + ruleId + ", name=" + name + ", description="
				+ description + ", priority=" + priority + ", isEnabled=" + isEnabled
				+ ", ruleSubject=" + ruleSubject + ", ruleAggregations=" + ruleAggregations + ", ruleSteps=" + ruleSteps + ", ruleEvents="
				+ ruleEvents + ", ruleActions=" + ruleActions + '}';
	}

}

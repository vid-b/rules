/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package com.sap.iot.ain.rules.models;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class ExecutableRuleJson {

    private String client;
    private String inviteeClient;

    private String ruleId;

    private String name;

    private String description;

    private String priority;

    private String isEnabled;

    private RuleSubject ruleSubject;

    private List<RuleAggregation> ruleAggregations;

    private List<RuleStep> ruleSteps;

    private List<RuleEvent> ruleEvents;

    private List<RuleAction> ruleActions;

    private String tenantId;

    private Instant fireTime;

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

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

    public RuleSubject getRuleSubject() {
        return ruleSubject;
    }

    public void setRuleSubject(RuleSubject ruleSubject) {
        this.ruleSubject = ruleSubject;
    }

    public List<RuleAggregation> getRuleAggregations() {
        return ruleAggregations;
    }

    public void setRuleAggregations(List<RuleAggregation> ruleAggregations) {
        this.ruleAggregations = ruleAggregations;
    }

    public List<RuleStep> getRuleSteps() {
        return ruleSteps;
    }

    public void setRuleSteps(List<RuleStep> ruleSteps) {
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

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public Instant getFireTime() {
        return fireTime;
    }

    public void setFireTime(Instant fireTime) {
        this.fireTime = fireTime;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.client);
        hash = 67 * hash + Objects.hashCode(this.ruleId);
        hash = 67 * hash + Objects.hashCode(this.name);
        hash = 67 * hash + Objects.hashCode(this.description);
        hash = 67 * hash + Objects.hashCode(this.priority);
        hash = 67 * hash + Objects.hashCode(this.isEnabled);
        hash = 67 * hash + Objects.hashCode(this.ruleSubject);
        hash = 67 * hash + Objects.hashCode(this.ruleAggregations);
        hash = 67 * hash + Objects.hashCode(this.ruleSteps);
        hash = 67 * hash + Objects.hashCode(this.ruleEvents);
        hash = 67 * hash + Objects.hashCode(this.ruleActions);
        hash = 67 * hash + Objects.hashCode(this.tenantId);
        hash = 67 * hash + Objects.hashCode(this.fireTime);
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
        final ExecutableRuleJson other = (ExecutableRuleJson) obj;
        if (!Objects.equals(this.client, other.client)) {
            return false;
        }
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
        if (!Objects.equals(this.tenantId, other.tenantId)) {
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
        if (!Objects.equals(this.fireTime, other.fireTime)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ExecutableRuleJsonNew{" + "client=" + client + ", ruleId=" + ruleId + ", name=" + name + ", description=" + description + ", priority=" + priority + ", isEnabled=" + isEnabled + ", ruleSubject=" + ruleSubject + ", ruleAggregations=" + ruleAggregations + ", ruleSteps=" + ruleSteps + ", ruleEvents=" + ruleEvents + ", ruleActions=" + ruleActions + ", tenantId=" + tenantId + ", fireTime=" + fireTime + '}';
    }

	public String getInviteeClient() {
		return inviteeClient;
	}

	public void setInviteeClient(String inviteeClient) {
		this.inviteeClient = inviteeClient;
	}

}

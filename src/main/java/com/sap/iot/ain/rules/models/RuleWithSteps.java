package com.sap.iot.ain.rules.models;

import java.util.List;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sap.iot.ain.rules.validation.RulesPOSTValiadtions;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@RulesPOSTValiadtions
public class RuleWithSteps {

    public Rule rule;

    public List<RuleStep> ruleSteps;
    
    @Valid
    public RuleSubject ruleSubject;
    
    public List<RuleAggregation> ruleAggregations;

    public List<RuleEvent> ruleEvents;    	

	public String schedule;

    public List<RuleAction> ruleActions;
    public RuleDescription ruleDescription;
    public String source;
    public Boolean isInvitee;

	public List<RuleAction> getRuleActions() {
		return ruleActions;
	}

	public void setRuleActions(List<RuleAction> ruleActions) {
		this.ruleActions = ruleActions;
	}
	
	public List<RuleEvent> getRuleEvents() {
		return ruleEvents;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
    
}

package com.sap.iot.ain.rules.models;

import java.util.List;

public class RuleWithStepsIn {

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

    public String getIsActive() {
        return isActive;
    }

    public void setIsActive(String isActive) {
        this.isActive = isActive;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public RuleSubjectOld getRuleSubject() {
        return ruleSubject;
    }

    public void setRuleSubjects(RuleSubjectOld ruleSubject) {
        this.ruleSubject = ruleSubject;
    }

    public THEN getTHEN() {
        return THEN;
    }

    public void setTHEN(THEN THEN) {
        this.THEN = THEN;
    }

    public Object getIF() {
        return IF;
    }

    public void setIF(Object IF) {
        this.IF = IF;
    }

    public String getRunSchedule() {
        return runSchedule;
    }

    public void setRunSchedule(String run_Schedule) {
        this.runSchedule = run_Schedule;
    }

    public String getNotRunSchedule() {
        return notRunSchedule;
    }

    public void setNotRunSchedule(String notRun_Schedule) {
        this.notRunSchedule = notRun_Schedule;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String name;
    public String description;
    public String priority;
    public String isActive;
    public String event;
    public String runSchedule;
    public String notRunSchedule;
    public String payload;
    public RuleSubjectOld ruleSubject;
    public Object IF;
    public THEN THEN;
}

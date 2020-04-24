/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sap.iot.ain.rules.models;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "\"sap.ain.metaData::rules.RULE_WITH_EVENTS\"")
@XmlRootElement
public class RuleWithEvents implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "\"ID\"", nullable = true, length = 100)
    private String id;

    @Column(name = "\"Name\"", length = 100)
    private String name;

    @Column(name = "\"RuleType\"", length = 10)
    private String ruleType;

    @Column(name = "\"Description\"", length = 200)
    private String description;

    @Column(name = "\"Priority\"", length = 100)
    private String priority;

    @Column(name = "\"IsEnabled\"", length = 10)
    private String isEnabled;

    //@Column(name="\"GROUPBY_FIELDS\"", length=400)
    //private String groupByFields;
    @Column(name = "\"EventID\"", length = 200)
    private String eventID;

    @Column(name = "\"EventName\"", length = 500)
    private String eventName;

    public RuleWithEvents(String iD, String name, String ruleType, String description, String priority, String isEnabled, String eventID, String eventName) {
        this.id = iD;
        this.name = name;
        this.ruleType = ruleType;
        this.description = description;
        this.priority = priority;
        this.isEnabled = isEnabled;
        this.eventID = eventID;
        this.eventName = eventName;
    }

    public RuleWithEvents() {
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getPriority() {
        return priority;
    }


    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }

    public String getRuleType() {
        return ruleType;
    }


    public void setIsEnabled(String isEnabled) {
        this.isEnabled = isEnabled;
    }

    public String getIsEnabled() {
        return isEnabled;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public String getEventID() {
        return eventID;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventName() {
        return eventName;
    }


    @Override
    public boolean equals(Object other) {
        if (null == other) {
            return false;
        } else if (this == other) {
            return true;
        } else if (!(other instanceof RuleWithEvents)) {
            return false;
        }
        RuleWithEvents cast = (RuleWithEvents) other;
        return false;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + this.id.hashCode();
        return result;
    }

}

package com.sap.iot.ain.rules.models;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import javax.persistence.IdClass;

@Entity
@Table(name = "\"sap.ain.metaData::Rules.RuleExecutionLogs\"")
@IdClass(RuleLogPK.class)
public class RuleLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "\"Client\"", nullable = true, length = 32)
    private String client;

    @Id
    @Column(name = "\"ID\"", nullable = false, length = 32)
    private String id;

    @Column(name = "\"Event\"", nullable = false, length = 500)
    private String event;

    @Column(name = "\"RuleID\"", nullable = false, length = 32)
    private String ruleId;

    @Column(name = "\"RuleName\"", nullable = false, length = 255)
    private String ruleName;

    @Column(name = "\"Phase\"", nullable = false, length = 200)
    private String phase;

    @Column(name = "\"System\"", nullable = false, length = 100)
    private String system;

    @Column(name = "\"Message\"", nullable = false, length = 4096)
    private String message;

    @Column(name = "\"SubjectID\"", nullable = false, length = 32)
    private String subjectId;

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

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

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + Objects.hashCode(this.client);
        hash = 41 * hash + Objects.hashCode(this.id);
        hash = 41 * hash + Objects.hashCode(this.event);
        hash = 41 * hash + Objects.hashCode(this.ruleId);
        hash = 41 * hash + Objects.hashCode(this.ruleName);
        hash = 41 * hash + Objects.hashCode(this.phase);
        hash = 41 * hash + Objects.hashCode(this.system);
        hash = 41 * hash + Objects.hashCode(this.message);
        hash = 41 * hash + Objects.hashCode(this.subjectId);
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
        final RuleLog other = (RuleLog) obj;
        if (!Objects.equals(this.client, other.client)) {
            return false;
        }
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.event, other.event)) {
            return false;
        }
        if (!Objects.equals(this.ruleId, other.ruleId)) {
            return false;
        }
        if (!Objects.equals(this.ruleName, other.ruleName)) {
            return false;
        }
        if (!Objects.equals(this.phase, other.phase)) {
            return false;
        }
        if (!Objects.equals(this.system, other.system)) {
            return false;
        }
        if (!Objects.equals(this.message, other.message)) {
            return false;
        }
        if (!Objects.equals(this.subjectId, other.subjectId)) {
            return false;
        }
        return true;
    }
    

    @Override
    public String toString() {
        return "RuleLog{" + "client=" + client + ", id=" + id + ", event=" + event + ", ruleId=" + ruleId + ", ruleName=" + ruleName + ", phase=" + phase + ", system=" + system + ", message=" + message + ", subjectId=" + subjectId + '}';
    }

  
}

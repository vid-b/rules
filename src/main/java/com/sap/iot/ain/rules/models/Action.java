package com.sap.iot.ain.rules.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


import java.io.Serializable;
import java.util.Objects;
import javax.persistence.IdClass;

@Entity
@IdClass(ActionPK.class)
@Table(name = "\"sap.ain.metaData::Rules.Action\"")
public class Action implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "\"Client\"", nullable = true, length = 32)
    private String client;

    @Id
    @Column(name = "\"ActionID\"", nullable = true, length = 32)
    private String actionID;

    @Column(name = "\"ActionName\"", nullable = true, length = 255)
    private String actionName;

    @Column(name = "\"Description\"", nullable = true, length = 200)
    private String description;

    @Column(name = "\"ActionType\"", nullable = true, length = 20)
    private String actionType;

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getActionID() {
        return actionID;
    }

    public void setActionID(String actionID) {
        this.actionID = actionID;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 73 * hash + Objects.hashCode(this.client);
        hash = 73 * hash + Objects.hashCode(this.actionID);
        hash = 73 * hash + Objects.hashCode(this.actionName);
        hash = 73 * hash + Objects.hashCode(this.description);
        hash = 73 * hash + Objects.hashCode(this.actionType);
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
        final Action other = (Action) obj;
        if (!Objects.equals(this.client, other.client)) {
            return false;
        }
        if (!Objects.equals(this.actionID, other.actionID)) {
            return false;
        }
        if (!Objects.equals(this.actionName, other.actionName)) {
            return false;
        }
        if (!Objects.equals(this.description, other.description)) {
            return false;
        }
        if (!Objects.equals(this.actionType, other.actionType)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Action{" + "client=" + client + ", actionID=" + actionID + ", actionName=" + actionName + ", description=" + description + ", actionType=" + actionType + '}';
    }
    
}

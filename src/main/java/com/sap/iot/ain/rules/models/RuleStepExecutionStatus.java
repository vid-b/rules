/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sap.iot.ain.rules.models;

import java.util.Objects;


public class RuleStepExecutionStatus {
    
    private String equipmentId;
    
    private boolean status;

    public String getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(String equipmentId) {
        this.equipmentId = equipmentId;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.equipmentId);
        hash = 59 * hash + (this.status ? 1 : 0);
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
        final RuleStepExecutionStatus other = (RuleStepExecutionStatus) obj;
        if (this.status != other.status) {
            return false;
        }
        if (!Objects.equals(this.equipmentId, other.equipmentId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "RuleStepExecutionStatus{" + "equipmentId=" + equipmentId + ", status=" + status + '}';
    }
    
}

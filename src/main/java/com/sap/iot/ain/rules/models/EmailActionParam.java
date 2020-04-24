/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sap.iot.ain.rules.models;

import java.util.List;
import java.util.Objects;


public class EmailActionParam {
    
    private List<EmailRecipient> to;

    public List<EmailRecipient> getTo() {
        return to;
    }

    public void setTo(List<EmailRecipient> to) {
        this.to = to;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 61 * hash + Objects.hashCode(this.to);
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
        final EmailActionParam other = (EmailActionParam) obj;
        if (!Objects.equals(this.to, other.to)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "EmailActionParam{" + "to=" + to + '}';
    }
    
}

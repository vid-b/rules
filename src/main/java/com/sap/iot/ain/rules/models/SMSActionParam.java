/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sap.iot.ain.rules.models;

import java.util.List;
import java.util.Objects;


public class SMSActionParam {
    
	private String to;
	
    public void setTo(String to) {
		this.to = to;
	}

    public String getTo() {
		return to;
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
        final SMSActionParam other = (SMSActionParam) obj;
        if (!Objects.equals(this.to, other.to)) {
            return false;
        }
        return true;
    }
    
}

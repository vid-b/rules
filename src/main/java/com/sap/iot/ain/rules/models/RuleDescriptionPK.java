package com.sap.iot.ain.rules.models;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Id;

public class RuleDescriptionPK {
	@Id
	@Column(name = "\"Client\"", length = 32)
	private String client;
	
	@Id
	@Column(name = "\"RuleID\"", length = 32)
	private String ruleID;

	@Id
	@Column(name = "\"LanguageISOCode\"", length = 6)
    private String languageISOCode;
	
	public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getRuleID() {
        return ruleID;
    }

    public void setRuleID(String ruleID) {
        this.ruleID = ruleID;
    }

    public String getLanguageISOCode() {
        return languageISOCode;
    }

    public void setLanguageISOCode(String languageISOCode) {
        this.languageISOCode = languageISOCode;
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.client);
        hash = 47 * hash + Objects.hashCode(this.ruleID);
        hash = 47 * hash + Objects.hashCode(this.languageISOCode);
        
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
        final RuleDescriptionPK other = (RuleDescriptionPK) obj;
        if (!Objects.equals(this.ruleID, other.ruleID)) {
            return false;
        }
        if (!Objects.equals(this.languageISOCode, other.languageISOCode)) {
            return false;
        }
       
        if (!Objects.equals(this.client, other.client)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "RuleDescription{" + "ruleID=" + ruleID + ", languageISOCode=" + languageISOCode + '}';
    }

}
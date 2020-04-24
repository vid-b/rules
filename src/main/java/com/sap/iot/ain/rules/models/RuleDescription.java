package com.sap.iot.ain.rules.models;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "\"sap.ain.metaData::Rules.RuleDescription\"")
@IdClass(RuleDescriptionPK.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RuleDescription {
	
	@Id
	@Column(name = "\"Client\"", length = 32)
	private String client;
	
	@Id
	@Column(name = "\"RuleID\"", length = 32)
	private String ruleID;

	@Id
	@Column(name = "\"LanguageISOCode\"", length = 6)
    private String languageISOCode;
    
	@Column(name = "\"Long\"", length = 500)
	private String longDescription;
    
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
    
    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.client);
        hash = 47 * hash + Objects.hashCode(this.ruleID);
        hash = 47 * hash + Objects.hashCode(this.languageISOCode);
        hash = 47 * hash + Objects.hashCode(this.longDescription);
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
        final RuleDescription other = (RuleDescription) obj;
        if (!Objects.equals(this.ruleID, other.ruleID)) {
            return false;
        }
        if (!Objects.equals(this.languageISOCode, other.languageISOCode)) {
            return false;
        }
        if (!Objects.equals(this.longDescription, other.longDescription)) {
            return false;
        }
        if (!Objects.equals(this.client, other.client)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "RuleDescription{" + "ruleID=" + ruleID + ", languageISOCode=" + languageISOCode + ", longDescription=" + longDescription + '}';
    }


}

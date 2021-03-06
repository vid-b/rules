/* 
 * This class was generated by the JDS command line tool.
 * 
 * It was created by the user SBSS_70962460707363720404167877752465397693708628039895110251133325240
 * on the HANA system mo-70f279fea.mo.sap.corp:30015 
 * for CDS entities contained inside the schema HNR2ZDOEHEYYQMO8_CONNECTED_GOODS_HDI_CONTAINER.
 * 
 * Time of creation: 2017/02/13 10:41:03
 * 
 */
package com.sap.iot.ain.rules.models;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sap.iot.ain.reuse.annotation.NotNull;
import com.sap.iot.ain.validation.utils.Order;

@Entity
@Table(name = "\"sap.ain.metaData::Rules.RuleSubject\"")
@IdClass(RuleSubjectPK.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RuleSubject implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "\"Client\"", nullable = true, length = 32)
    private String client;

    @Id
    @Column(name = "\"ID\"", nullable = false, length = 100)
    private String id;

    @Column(name = "\"RuleID\"", length = 200)
    private String ruleID;

    @NotNull(objectName = "subjectID", groups = Order.Level1.class)
    @Column(name = "\"SubjectID\"", length = 200)
    private String subjectID;

    @Column(name = "\"SubjectExternalID\"", length = 200)
    private String subjectExternalId;

    @Column(name = "\"SubjectType\"", length = 200)
    private String subjectType;

    @Transient
    private Instant timestamp;

    @Transient
    private List<Equipment> equipments;

    @Transient
    private Model model;

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

    public String getRuleID() {
        return ruleID;
    }

    public void setRuleID(String ruleID) {
        this.ruleID = ruleID;
    }

    public String getSubjectID() {
        return subjectID;
    }

    public void setSubjectID(String subjectID) {
        this.subjectID = subjectID;
    }

    public String getSubjectExternalId() {
        return subjectExternalId;
    }

    public void setSubjectExternalId(String subjectExternalId) {
        this.subjectExternalId = subjectExternalId;
    }

    public String getSubjectType() {
        return subjectType;
    }

    public void setSubjectType(String subjectType) {
        this.subjectType = subjectType;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public List<Equipment> getEquipments() {
        return equipments;
    }

    public void setEquipments(List<Equipment> equipments) {
        this.equipments = equipments;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + Objects.hashCode(this.client);
        hash = 17 * hash + Objects.hashCode(this.id);
        hash = 17 * hash + Objects.hashCode(this.ruleID);
        hash = 17 * hash + Objects.hashCode(this.subjectID);
        hash = 17 * hash + Objects.hashCode(this.subjectExternalId);
        hash = 17 * hash + Objects.hashCode(this.subjectType);
        hash = 17 * hash + Objects.hashCode(this.timestamp);
        hash = 17 * hash + Objects.hashCode(this.equipments);
        hash = 17 * hash + Objects.hashCode(this.model);
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
        final RuleSubject other = (RuleSubject) obj;
        if (!Objects.equals(this.client, other.client)) {
            return false;
        }
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.ruleID, other.ruleID)) {
            return false;
        }
        if (!Objects.equals(this.subjectID, other.subjectID)) {
            return false;
        }
        if (!Objects.equals(this.subjectExternalId, other.subjectExternalId)) {
            return false;
        }
        if (!Objects.equals(this.subjectType, other.subjectType)) {
            return false;
        }
        if (!Objects.equals(this.timestamp, other.timestamp)) {
            return false;
        }
        if (!Objects.equals(this.equipments, other.equipments)) {
            return false;
        }
        if (!Objects.equals(this.model, other.model)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "RuleSubjectNew{" + "client=" + client + ", id=" + id + ", ruleID=" + ruleID + ", subjectID=" + subjectID + ", subjectExternalId=" + subjectExternalId + ", subjectType=" + subjectType + ", timestamp=" + timestamp + ", equipments=" + equipments + ", model=" + model + '}';
    }
    

}

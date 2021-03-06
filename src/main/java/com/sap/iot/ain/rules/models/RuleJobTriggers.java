/* 
 * This class was generated by the JDS command line tool.
 * 
 * It was created by the user SBSS_19220038074936318185415652640319701600333637668158810727543328814
 * on the HANA system mo-d34b9d109:30015 
 * for CDS entities contained inside the schema 3F15CB45A62849F8834FFBA2B4C866CB.
 * 
 * Time of creation: 2016/09/22 14:17:54
 * 
 */
package com.sap.iot.ain.rules.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import javax.persistence.IdClass;

@Entity
@Table(name = "\"sap.ain.metaData::Rules.RuleJobTriggers\"")
@IdClass(RuleJobTriggersPK.class)
public class RuleJobTriggers implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "\"Client\"", nullable = true, length = 32)
    private String client; 

    @Id
    @Column(name = "\"ID\"", nullable = false, length = 32)
    private String id;

    @Column(name = "\"Name\"", length = 200)
    private String name;

    @Column(name = "\"CronExpression\"", length = 200)
    private String cronexpression;

    @Column(name = "\"CronExpressionNotRul\"", length = 200)
    private String cronexpressionNotRun;

    @Column(name = "\"JobID\"", length = 200)
    private String jobId;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCronexpression() {
        return cronexpression;
    }

    public void setCronexpression(String cronexpression) {
        this.cronexpression = cronexpression;
    }

    public String getCronexpressionNotRun() {
        return cronexpressionNotRun;
    }

    public void setCronexpressionNotRun(String cronexpressionNotRun) {
        this.cronexpressionNotRun = cronexpressionNotRun;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + Objects.hashCode(this.client);
        hash = 13 * hash + Objects.hashCode(this.id);
        hash = 13 * hash + Objects.hashCode(this.name);
        hash = 13 * hash + Objects.hashCode(this.cronexpression);
        hash = 13 * hash + Objects.hashCode(this.cronexpressionNotRun);
        hash = 13 * hash + Objects.hashCode(this.jobId);
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
        final RuleJobTriggers other = (RuleJobTriggers) obj;
        if (!Objects.equals(this.client, other.client)) {
            return false;
        }
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.cronexpression, other.cronexpression)) {
            return false;
        }
        if (!Objects.equals(this.cronexpressionNotRun, other.cronexpressionNotRun)) {
            return false;
        }
        if (!Objects.equals(this.jobId, other.jobId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "RuleJobTriggers{" + "client=" + client + ", id=" + id + ", name=" + name + ", cronexpression=" + cronexpression + ", cronexpressionNotRun=" + cronexpressionNotRun + ", jobId=" + jobId + '}';
    }

}

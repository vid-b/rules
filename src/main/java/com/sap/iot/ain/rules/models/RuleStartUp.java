package com.sap.iot.ain.rules.models;

public class RuleStartUp {

	private String id;
	private String cronexpression;
	private String tenantSubDomain;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}


	public String getCronexpression() {
		return cronexpression;
	}

	public void setCronexpression(String cronexpression) {
		this.cronexpression = cronexpression;
	}

	public String getTenantSubDomain() {
		return tenantSubDomain;
	}

	public void setTenantSubDomain(String tenantSubDomain) {
		this.tenantSubDomain = tenantSubDomain;
	}

}

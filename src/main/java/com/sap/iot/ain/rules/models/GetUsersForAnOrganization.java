package com.sap.iot.ain.rules.models;

import javax.persistence.Column;

public class GetUsersForAnOrganization {

	@Column(name = "\"PersonID\"")
	private String personId;

	@Column(name = "\"UserID\"")
	private String userId;

	@Column(name = "\"GivenName\"")
	private String givenName;

	@Column(name = "\"FamilyName\"")
	private String familyName;

	@Column(name = "\"EmailAddress\"")
	private String emailAddress;

	@Column(name = "\"OrganizationID\"")
	private String organizationId;

	@Column(name = "\"AccountType\"")
	private String accountType;

	@Column(name = "\"HCPAccountDetails\"")
	private String hcpAccountDetails;

	@Column(name = "\"OrganizationName\"")
	private String organizationName;

	@Column(name = "\"ParentOrgID\"")
	private String parentOrgId;

	@Column(name = "\"ParentOrganizationName\"")
	private String parentOrganizationName;

	@Column(name = "\"CreatedBy\"")
	private String createdBy;

	@Column(name = "\"CreatedOn\"")
	private String createdOn;

	@Column(name = "\"ChangedBy\"")
	private String changedBy;

	@Column(name = "\"ChangedOn\"")
	private String changedOn;

	@Column(name = "\"Scope\"")
	private String scope;

	@Column(name = "\"SourceSearchTerms\"")
	private String sourceSearchTerms;

	@Column(name = "\"isOwn\"")
	private String isOwn;

	@Column(name = "\"FullName\"")
	private String fullName;

	public String getPersonId() {
		return personId;
	}

	public void setPersonId(String personId) {
		this.personId = personId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getGivenName() {
		return givenName;
	}

	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	public String getFamilyName() {
		return familyName;
	}

	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getOrganizationId() {
		return organizationId;
	}

	public void setOrganizationId(String organizationId) {
		this.organizationId = organizationId;
	}

	public String getAccountType() {
		return accountType;
	}

	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}

	public String getHcpAccountDetails() {
		return hcpAccountDetails;
	}

	public void setHcpAccountDetails(String hcpAccountDetails) {
		this.hcpAccountDetails = hcpAccountDetails;
	}

	public String getOrganizationName() {
		return organizationName;
	}

	public void setOrganizationName(String organizationName) {
		this.organizationName = organizationName;
	}

	public String getParentOrgId() {
		return parentOrgId;
	}

	public void setParentOrgId(String parentOrgId) {
		this.parentOrgId = parentOrgId;
	}

	public String getParentOrganizationName() {
		return parentOrganizationName;
	}

	public void setParentOrganizationName(String parentOrganizationName) {
		this.parentOrganizationName = parentOrganizationName;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(String createdOn) {
		this.createdOn = createdOn;
	}

	public String getChangedBy() {
		return changedBy;
	}

	public void setChangedBy(String changedBy) {
		this.changedBy = changedBy;
	}

	public String getChangedOn() {
		return changedOn;
	}

	public void setChangedOn(String changedOn) {
		this.changedOn = changedOn;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getSourceSearchTerms() {
		return sourceSearchTerms;
	}

	public void setSourceSearchTerms(String sourceSearchTerms) {
		this.sourceSearchTerms = sourceSearchTerms;
	}

	public String getIsOwn() {
		return isOwn;
	}

	public void setIsOwn(String isOwn) {
		this.isOwn = isOwn;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}



}

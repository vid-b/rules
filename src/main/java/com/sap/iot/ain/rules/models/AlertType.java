package com.sap.iot.ain.rules.models;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AlertType implements RowMapper<AlertType> {

	private String id;
	
	private String alertTypeId;

	private String externalId;

	private Integer severity;

	private String errorCode;
	
	private String tenantSubDomain;

	private String alertTypeDescription;
	
	private static final Logger logger = LoggerFactory.getLogger(AlertType.class);

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAlertTypeId() {
		return alertTypeId;
	}

	public void setAlertTypeId(String alertTypeId) {
		this.alertTypeId = alertTypeId;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	
	public Integer getSeverity() {
		return severity;
	}

	public void setSeverity(Integer severity) {
		this.severity = severity;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getTenantSubDomain() {
		return tenantSubDomain;
	}

	public void setTenantSubDomain(String tenantSubDomain) {
		this.tenantSubDomain = tenantSubDomain;
	}

	public String getAlertTypeDescription() {
		return alertTypeDescription;
	}

	public void setAlertTypeDescription(String alertTypeDescription) {
		this.alertTypeDescription = alertTypeDescription;
	}

	@Override
	public AlertType mapRow(ResultSet rs, int rowNum) throws SQLException {
		AlertType alertType = null;

		try {
			alertType = this.getClass().newInstance();
			alertType.setId(rs.getString("ID"));
			alertType.setAlertTypeId(rs.getString("AlertTypeID"));
			alertType.setExternalId(rs.getString("ExternalID"));
			alertType.setSeverity(rs.getInt("Severity"));
			alertType.setErrorCode(rs.getString("ErrorCode"));
			alertType.setTenantSubDomain(rs.getString("TenantSubDomain"));
			alertType.setAlertTypeDescription(rs.getString("Description"));
		} catch (SQLException | IllegalAccessException | InstantiationException e) {
			logger.error("Exception occured while fetching alert type");
		}
		return alertType;
	}
}



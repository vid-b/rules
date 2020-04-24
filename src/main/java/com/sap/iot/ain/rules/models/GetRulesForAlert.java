package com.sap.iot.ain.rules.models;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetRulesForAlert implements RowMapper<GetRulesForAlert> {
	
    private String ruleId;
	
	private String alertTypeId;
	private String client;
	
	public String getRuleId() {
		return ruleId;
	}

	public void setRuleId(String ruleId) {
		this.ruleId = ruleId;
	}

	public String getAlertTypeId() {
		return alertTypeId;
	}

	public void setAlertTypeId(String alertTypeId) {
		this.alertTypeId = alertTypeId;
	}

	private static final Logger logger = LoggerFactory.getLogger(GetRulesForAlert.class);
	
	@Override
	public GetRulesForAlert mapRow(ResultSet rs, int rowNum) throws SQLException {
		GetRulesForAlert getRulesForAlert = null;

		try {
			getRulesForAlert = this.getClass().newInstance();
			getRulesForAlert.setRuleId(rs.getString("RuleID"));
			getRulesForAlert.setAlertTypeId(rs.getString("AlertTypeID"));
			getRulesForAlert.setClient(rs.getString("Client"));
		} catch (SQLException | IllegalAccessException | InstantiationException e) {
			logger.error("Exception occured while fetching alert type");
		}
		return getRulesForAlert;
	}

	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}

}

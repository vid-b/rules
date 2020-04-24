package com.sap.iot.ain.rules.validation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.sap.iot.ain.acl.core.Authorization;
import com.sap.iot.ain.auth.core.AINAccessControlList;
import com.sap.iot.ain.core.AINObjectTypes;
import com.sap.iot.ain.reuse.payload.ID;
import com.sap.iot.ain.rules.models.RuleAction;
import com.sap.iot.ain.security.AuthenticatedUserDetails;
import com.sap.iot.ain.reuse.utils.ObjectUtils;

public class ValidationHelperDao {

	@Autowired
	private AuthenticatedUserDetails aud;
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private AINAccessControlList acl;

	public static final int ID_CHUNK_SIZE = 150;

	public ValidationHelperDao() {
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
	}

	public boolean isRuleNameValid(String name) {
		List<String> ruleNames = new ArrayList<>();
		String query = "select \"Name\" from \"AIN_DEV\".\"sap.ain.metaData::Rules.Rule\" where \"Client\" = ? and \"Name\" = ?";
		Object[] params = new Object[] {aud.getUserDetails().getUserClientId(), name};
		ruleNames = jdbcTemplate.query(query, params, (rs, rowNum) -> rs.getString("Name"));
		if (ruleNames.isEmpty()) {

			return false;
		}
		return true;
	}
	
	public boolean isRuleNameChanged(String name, String ruleId) {
		List<String> ruleNames = new ArrayList<>();
		String query = "select \"Name\" from \"AIN_DEV\".\"sap.ain.metaData::Rules.Rule\" where \"Client\" = ? and \"Name\" = ? and \"ID\" = ?";
		Object[] params = new Object[] {aud.getUserDetails().getUserClientId(), name, ruleId};
		ruleNames = jdbcTemplate.query(query, params, (rs, rowNum) -> rs.getString("Name"));
		if (ruleNames.isEmpty()) {

			return false;
		}
		return true;
	}


	public boolean isRuleAlertTypePresent(String id) {
		// TODO Auto-generated method stub
		List<String> ruleActionIds = new ArrayList<>();
		String query = "select \"RuleID\" from \"AIN_DEV\".\"sap.ain.metaData::Rules.RuleSubject\" \"Subject\""
				+"where  \"Subject\".\"RuleID\" in(select \"Action\".\"RuleID\" from"
				+"\"AIN_DEV\".\"sap.ain.metaData::Rules.RuleAction\" \"Action\" where \"Action\".\"RuleID\" = ?"
				+"and \"Action\".\"ActionID\" = 'alerttype') and \"Subject\".\"SubjectType\" in (0,1)";


		Object[] params = new Object[] {id};
		ruleActionIds = jdbcTemplate.query(query, params, (rs, rowNum) -> rs.getString("RuleID"));

		if (ruleActionIds.isEmpty()) {

			return false;
		}

		return true;
	}

	public boolean isAlertExternalIDValid(String id) {
		// TODO Auto-generated method stub
		List<String> externalIds = new ArrayList<>();
		String query = "select \"Header\".\"ExternalID\" from" + 
				"\"AIN_DEV\".\"sap.ain.metaData::AlertType.Header\" \"Header\"  where \"Header\".\"ID\" in" + 
				"(select \"AlertTypeID\" from \"AIN_DEV\".\"sap.ain.metaData::Rules.RuleAction\" \"Action\" where \"Action\".\"RuleID\" = ?" + 
				"and \"Action\".\"ActionID\" = 'alerttype')";


		Object[] params = new Object[] {id};
		externalIds = jdbcTemplate.query(query, params, (rs, rowNum) -> rs.getString("ExternalID"));

		if (externalIds.isEmpty()) {

			return false;
		}

		return true;
	}


	public boolean checkObjectAuthorization(List<String> objects, String objectType) {
		ID id;
		String obj;
		List<ID> objAuth = new ArrayList<ID>();
		List<String> validObjects = new ArrayList<String>();
		List<Authorization> auth = new ArrayList<Authorization>();
		for (String oObject : objects) {
			id = new ID();
			id.setId(oObject);
			objAuth.add(id);
		}
		auth = acl.hasPermission(objAuth);
		for (Authorization oAuth : auth) {

			if ((oAuth.hasWrite() || oAuth.isOwner() || oAuth.hasRead())
					&& oAuth.getConsumePrivilege().equals("1")
					&& oAuth.getObjectType().equals(objectType)) {
				obj = new String();
				obj = oAuth.getObjectId();
				validObjects.add(obj);
			}

		}

		if (objects.size() == validObjects.size()) {
			return true;
		} else {

			return false;
		}

	}


	public boolean isInsModValid(String subjectId, String ruleId) {
		// TODO Auto-generated method stub
		List<Integer> subjectType = new ArrayList<>();
		List<String> objects = new ArrayList<>();
		String query = "select \"SubjectType\" from \"AIN_DEV\".\"sap.ain.metaData::Rules.RuleSubject\" where \"RuleID\" = ? and \"SubjectID\" = ?";
		Object[] params = new Object[] {ruleId,subjectId};
		subjectType = jdbcTemplate.query(query, params, (rs, rowNum) -> rs.getInt("SubjectType"));
		if(subjectType.isEmpty()) {
			return false;
		}else {
			if(subjectType.get(0) == 0) {
				objects.add(subjectId);
				return checkObjectAuthorization(objects,"MOD");
			}else if(subjectType.get(0) == 2) {
				objects.add(subjectId);
				return checkObjectAuthorization(objects,"INS");

			}else {
				return false;
			}
		}

	}

	public boolean isEquListValid(List<String> boList, String subjectId, String ruleId) {
		String query = "SELECT \"ID\" FROM  \"_SYS_BIC\".\"sap.ain.views.rule/RuleObjectAssignmentValidation\" "
				+ " (PLACEHOLDER.\"$$iv_equ_list$$\" => ?, " + " PLACEHOLDER.\"$$iv_ruleId$$\" => ?, "  + " PLACEHOLDER.\"$$iv_subjectId$$\" => ?); ";


		try {

			// Processing the list in chunks as HANA nvarchar limitation is 5000
			// (150 * 32 < 5000)
			for (int i = 0; i < boList.size(); i += ID_CHUNK_SIZE) {

				List<String> chunk = boList.subList(i, Math.min(boList.size(), i + ID_CHUNK_SIZE));

				Object[] viewParams = new Object[]{
						getIDsWithDelimiter(chunk, ","), ruleId, subjectId};

				List<String> invalidBOs = jdbcTemplate.query( query, viewParams, (rs, rowNum) -> 
				rs.getString("ID")

						);

				if (!invalidBOs.isEmpty()) {

					return false;
				}
			} 
			return true;
		} catch (DataAccessException e) {
			return false;
		}
	}


	public static String getIDsWithDelimiter(List<String> ids, String delimiter) {

		String idsWithComma = "";
		if (ids == null) {
			return idsWithComma;
		}

		for (int i = 0; i < ids.size(); i++) {
			if (i + 1 == ids.size()) {
				idsWithComma = idsWithComma + ids.get(i);
			} else {
				idsWithComma = idsWithComma + ids.get(i) + delimiter;
			}
		}

		return idsWithComma;
	}

}

package com.sap.iot.ain.rules.implementation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import com.sap.iot.ain.core.AINObjectTypes;
import com.sap.iot.ain.reuse.GenericRowMapper;
import com.sap.iot.ain.reuse.utils.ObjectUtils;
import com.sap.iot.ain.rules.models.AttributeValue;
import com.sap.iot.ain.rules.models.Equipment;
import com.sap.iot.ain.rules.models.ExecutableRuleJsonOld;
import com.sap.iot.ain.rules.models.GetUsersForAnOrganization;
import com.sap.iot.ain.rules.models.RuleAggregationOld;
import com.sap.iot.ain.security.AuthenticatedUserDetails;

@Component
public class RulesUtils {

	private static final Logger logger = LoggerFactory.getLogger(RulesUtils.class);
	
	@Autowired
	private JdbcTemplate jdbcTemplate;

	public List<AttributeValue> getAttributes(String clientId, String subjectId,
			String attributeId, String subjectType, String field1TemplateType,
			String field1Template, String field1TemplatePST) {
		List<AttributeValue> attributeValue = new ArrayList<>();
		try {
		String queryForAttributeValue = "";
		Object[] params = null;
		if(subjectType.equals("0") || (subjectType.equals("1") && field1TemplateType.equals("0"))) {
			queryForAttributeValue = 
				"Select \"ObjectValue\".\"Client\", \"ObjectValue\".\"ID\", \"ObjectValue\".\"PropertyID\", \"ObjectValue\".\"BooleanValue\", "
						+ " \"ObjectValue\".\"DateValue\", \"ObjectValue\".\"StringValue\" from \"AIN_DEV\".\"sap.ain.metaData::Object.Value\" "
						+ " as \"ObjectValue\" where \"ObjectValue\".\"ID\"= ? and "
						+ " \"ObjectValue\".\"PropertyID\"= ? and \"ObjectValue\".\"Type\"= ? "
						+ " and \"ObjectValue\".\"Client\"= ? and \"ObjectValue\".\"Version\"= "
						+ " (SELECT \"versionheader\".\"ModelVersion\" FROM \"sap.ain.metaData::Model.Header\" as "
						+ "  \"versionheader\" WHERE \"versionheader\".\"ID\" = \"ObjectValue\".\"ID\" and "
						+ " \"versionheader\".\"isMarkedForDeletion\" = '0' and \"versionheader\".\"Status\"='2') ";
			params = new Object[] {subjectId, attributeId, AINObjectTypes.MOD.toString(), clientId};
		} else if(subjectType.equals("1")) {
			queryForAttributeValue = 
					"Select \"ObjectValue\".\"Client\", \"ObjectValue\".\"ID\", \"ObjectValue\".\"PropertyID\", \"ObjectValue\".\"BooleanValue\", "
							+ " \"ObjectValue\".\"DateValue\", \"ObjectValue\".\"StringValue\" from \"AIN_DEV\".\"sap.ain.metaData::Object.Value\" "
							+ " as \"ObjectValue\" where \"ObjectValue\".\"ID\"= ? and "
							+ " \"ObjectValue\".\"PropertyID\"= ? and \"ObjectValue\".\"Type\"= ? "
							+ " and \"ObjectValue\".\"Client\"= ? and \"ObjectValue\".\"PSTID\" = ? and \"ObjectValue\".\"CategoryID\" = ?"
							+ " and \"ObjectValue\".\"Version\"=  (SELECT \"versionheader\".\"EquipmentVersion\" FROM \"sap.ain.metaData::Equipment.Header\" as "
							+ "  \"versionheader\" WHERE \"versionheader\".\"ID\" = \"ObjectValue\".\"ID\" and "
							+ " \"versionheader\".\"isMarkedForDeletion\" = '0' and \"versionheader\".\"Status\"='2') ";
			String templateType = AINObjectTypes.EQU.toString();
			params = new Object[] {subjectId, attributeId, templateType, clientId, field1TemplatePST, field1Template};
		}
		attributeValue = jdbcTemplate.query(queryForAttributeValue,
				params,
				new AttributeValue());
		attributeValue.forEach(attribute -> {
			logger.debug("Model Id: " + attribute.getModelId() + ", Attribute Id: "
					+ attribute.getAttributeId());
		});
		} catch (DataAccessException exception) {
			logger.error("Exception occured at RulesUtil.getAttributesForModel{} ",
					exception.getMessage());
		}
		return attributeValue;

	}

	public String getExternalSystemId(String clientId, String systemName) {
		String query =
				"SELECT top 1 \"ID\" FROM \"_SYS_BIC\".\"sap.ain.views/ExternalSystemsList\" (PLACEHOLDER.\"$$iv_client$$\" => ? ,PLACEHOLDER.\"$$iv_lang$$\"=>?)"
						+ " where \"SystemStatusDescription\" = 'Active' and \"SystemType\" = 'PdMS' and \"SystemName\" = ?";

		String extSysId = "";
		try {
			extSysId = jdbcTemplate.queryForObject(query, new Object[] {clientId, "en", systemName},
					String.class);
		} catch (DataAccessException exception) {
			logger.error("Exception occured at RulesUtil.getExternalSystemId{} ",
					exception.getMessage());
		}

		return extSysId;
	}

	public List<Equipment> getEquipmentListForARule(String ruleId, String systemId,
			String clientId) {
		List<Equipment> equipments = new ArrayList<>();
		try {
			String queryForRulesSubject =
					"Select * from \"_SYS_BIC\".\"sap.ain.views/EquipmentsForModel\""
							+ "(PLACEHOLDER.\"$$iv_client$$\" => ?,"
							+ " PLACEHOLDER.\"$$iv_rule_id$$\" => ?, PLACEHOLDER.\"$$iv_system_id$$\" => ?)";
			equipments = jdbcTemplate.query(queryForRulesSubject,
					new Object[] {clientId, ruleId, systemId}, new Equipment());

			logger.debug("RuleService.getEquipmentListForAModel() returns equipments : "
					+ equipments.size());
			equipments.forEach(equ -> {
				logger.debug("Equipmentname: " + equ.getEquipmentId() + ", External Id: "
						+ equ.getEquipmentExternalId());
			});
		} catch (DataAccessException exception) {
			logger.error("Exception occured at RulesUtil.getEquipmentListForARule{} ",
					exception.getMessage());
		}
		return equipments;
	}

	public Map<String, String> getExternalIndicatorIdsForAINObjectIds(String systemName,
			Set<String> ainIndicatorIds, String subjectId, String clientId, String filterParam) {
		String externalIdsForAINIndicatorIds =
				"Select DISTINCT(\"PROPERTY_ID\") as \"PROPERTY_ID\", \"ATTRIBUTE_ID\" from "
						+ " \"sap.ain.metaData::Configurations.ExternalSystemMapping\" where  \"CLIENT\" = ? and \"SYSTEM_NAME\"= ? and \"MAPPING_FLAG\"='0' "
						+ "and \"" + filterParam + "\"= ?  and \"ATTRIBUTE_ID\" in ("
						+ ObjectUtils.getQuestionMark(ainIndicatorIds.size()) + ")";

		ArrayList<String> idList = new ArrayList<>();
		idList.addAll(ainIndicatorIds);
		idList.add(0, subjectId);
		idList.add(0, systemName);
		idList.add(0, clientId);

		Map<String, String> externalIds = jdbcTemplate.query(externalIdsForAINIndicatorIds,
				idList.toArray(), new ResultSetExtractor<Map<String, String>>() {

					@Override
					public Map<String, String> extractData(ResultSet rs)
							throws SQLException, DataAccessException {
						Map<String, String> externalIds = new HashMap<>();
						while (rs.next()) {
							externalIds.put(rs.getString("ATTRIBUTE_ID"),
									rs.getString("PROPERTY_ID"));
						}
						return externalIds;
					}

				});
		return externalIds;
	}

	public void setExternalIdsForIndicator(ExecutableRuleJsonOld ruleJson, String clientId) {
		Set<String> indicators = new HashSet<>();
		ruleJson.getRuleSteps().forEach(step -> {
			if(step.getField1Type().equalsIgnoreCase("AGG")){
				Optional<RuleAggregationOld> optionalRuleaggregation = ruleJson.getRuleAggregations().stream().filter(aggregation -> aggregation.getName().equals(step.getField1())).findFirst();
				if(optionalRuleaggregation.isPresent())
				{
				    String indicatorFromAggregator = optionalRuleaggregation.get().getFieldID();
				    indicators.add(indicatorFromAggregator);
				}
			}
			if (step.getField1IsIndicator() != null && step.getField1IsIndicator().equals("X")) {
				indicators.add(step.getField1());
			}
			if (step.getField2IsIndicator() != null && step.getField2IsIndicator().equals("X")) {
				indicators.add(step.getField2());
			}
		});

		if (indicators == null || indicators.isEmpty()) {
			return;
		}
		
		String filterParam = ruleJson.getRuleSubject().getSubjectType() == "0"? "MODEL_ID" : "EQUIPMENT_ID";

		Map<String, String> externalIdsForAinIds = getExternalIndicatorIdsForAINObjectIds(
				"pdmsSysPackage", indicators, ruleJson.getRuleSubject().getSubjectID(), clientId, filterParam);

		if (externalIdsForAinIds == null || externalIdsForAinIds.isEmpty()) {
			return;
		}
		ruleJson.getRuleSteps().forEach(step -> {
			if(step.getField1Type().equalsIgnoreCase("AGG")){
				Optional<RuleAggregationOld> optionalRuleaggregation = ruleJson.getRuleAggregations().stream().filter(aggregation -> aggregation.getName().equals(step.getField1())).findFirst();
				if(optionalRuleaggregation.isPresent())
				{
					RuleAggregationOld aggregation = optionalRuleaggregation.get();
				    aggregation.setFieldExt(externalIdsForAinIds.get(aggregation.getFieldID()));
				}
			}
			if (step.getField1IsIndicator() != null && step.getField1IsIndicator().equals("X")) {
				step.setField1Ext(externalIdsForAinIds.get(step.getField1()));
				logger.debug("Field1 external id: " + externalIdsForAinIds.get(step.getField1()));
			}
			if (step.getField2IsIndicator() != null && step.getField2IsIndicator().equals("X")) {
				step.setField2Ext(externalIdsForAinIds.get(step.getField2()));
				logger.debug("Field2 external id: " + externalIdsForAinIds.get(step.getField2()));

			}
		});
	}

	public String getExternalIdForAINObjectId(String clientId, String ainObjectId,
			String systemName, String objectType) {
		String externalSysId = getExternalSystemId(clientId, systemName);

		String externalIdForModelIdQuery = " select \"ExternalIDMapping\".\"ExternalID\"   from "
				+ "\"sap.ain.metaData::Configurations.ExternalIDMapping\" as \"ExternalIDMapping\" where \"ExternalIDMapping\".\"Client\" = ?"
				+ "and \"ExternalIDMapping\".\"ID\" = ? and \"ExternalIDMapping\".\"ObjectType\" = ? and "
				+ " \"ExternalIDMapping\".\"AINObjectID\" = ?";

		String extModelId = "";
		try {
			extModelId = jdbcTemplate.queryForObject(externalIdForModelIdQuery,
					new Object[] {clientId, externalSysId, objectType, ainObjectId}, String.class);
		} catch (DataAccessException exception) {
			logger.error("Exception occured at RulesUtil.getExternalIdForAINObjectId{} ",
					exception.getMessage());
		}
		return extModelId;
	}

	public static String insertIntoEquipmentMappingQuery() {
		String query =
				"INSERT INTO \"sap.ain.metaData::Rules.RuleEquipmentMapping\"(\"Client\",\"RuleID\",\"EquipmentID\",\"ModelID\")"
						+ " VALUES (?,?,?,?)";
		return query;
	}

	public static String insertIntoRuleExecutionLogsQuery() {
		String query =
				"INSERT INTO \"sap.ain.metaData::Rules.RuleRunLogs\"(\"Client\",\"ID\",\"Event\",\"RuleID\",\"RuleName\",\"Phase\",\"System\",\"Message\",\"SubjectID\")"
						+ " VALUES (?,?,?,?,?,?,?,?,?)";
		return query;
	}

	public static String insertIntoRuleRunsQuery() {
		String query =
				"INSERT INTO \"sap.ain.metaData::Rules.RuleRun\"(\"Client\",\"ID\",\"RuleID\",\"SubjectID\",\"Timestamp\",\"HasResult\",\"ExecutionStatus\")"
						+ " VALUES (?,?,?,?,?,?,?)";
		return query;
	}
        
        public static String updateRuleRunsQuery() {
		String query =
				"Update \"sap.ain.metaData::Rules.RuleRun\" set \"ExecutionStatus\" =?, \"SubjectID\"=? where \"Client\" =? and \"ID\"=?";
		return query;
	}
        

	public List<GetUsersForAnOrganization> getUsersForAnOrganization(String clientId) {

		String getUsersForAnOrganizationQuery =
				"select * from \"_SYS_BIC\".\"sap.ain.views/GetUsersForAnOrganization\" \r\n"
						+ "(PLACEHOLDER.\"$$iv_client$$\" => ?,PLACEHOLDER.\"$$iv_lang$$\" => ?)";

		List<GetUsersForAnOrganization> getUsersForAnOrganization = new ArrayList<>();
		try {
			getUsersForAnOrganization = (List<GetUsersForAnOrganization>) jdbcTemplate.query(
					getUsersForAnOrganizationQuery,
					new Object[] {clientId,
							AuthenticatedUserDetails.getInstance().getUserDetails().getLocale()
									.getLanguage()},
					new GenericRowMapper<GetUsersForAnOrganization>(
							GetUsersForAnOrganization.class));
		} catch (DataAccessException exception) {
			logger.error("Exception occured at RulesUtil.getUsersForAnOrganization{} ",
					exception.getMessage());
		}

		return getUsersForAnOrganization;
	}

	public String getExternalIDforIndicator(String attributeId, String systemName, String modelId,
			String clientId) {
		String indicatorQuery =
				"Select DISTINCT(\"PROPERTY_ID\") from \"sap.ain.metaData::Configurations.ExternalSystemMapping\" where \"ATTRIBUTE_ID\"= ? and \"SYSTEM_NAME\"= ? and \"MAPPING_FLAG\"= '0' "
						+ " and \"MODEL_ID\"= ? and \"CLIENT\" = ?";
		String indicator = "";
		try {
			indicator = jdbcTemplate.queryForObject(indicatorQuery,
					new Object[] {attributeId, systemName, modelId, clientId}, String.class);
		} catch (DataAccessException exception) {
			logger.error("Exception occured at RulesUtil.getExternalIDforIndicator{} ",
					exception.getMessage());
		}
		return indicator;
	}

	public String getNamedPSTId(String groupId, String systemName, String modelId,
			String clientId) {
		String pstQuery =
				"Select DISTINCT(\"NAMED_PST_ID\") as \"NAMED_PST_ID\" from \"sap.ain.metaData::Configurations.ExternalSystemMapping\" where "
						+ " \"GROUP_ID\"= ? and \"SYSTEM_NAME\"= ?  and \"MAPPING_FLAG\"='0' and \"CLIENT\" = ?"
						+ "and \"MODEL_ID\"= ?";
		String pst = "";
		try {
			pst = jdbcTemplate.queryForObject(pstQuery,
					new Object[] {groupId, systemName, clientId, modelId}, String.class);
		} catch (DataAccessException exception) {
			logger.error("Exception occured at RulesUtil.getNamedPSTId{} ", exception.getMessage());
		}
		return pst;
	}

	public String getPSTName(String groupId, String systemName, String subjectId, String clientId, String subjectType, Boolean isModelsIndicator) {
		String pstQuery1 =
				"Select DISTINCT(\"PST_NAME\") as \"PST_NAME\" from \"sap.ain.metaData::Configurations.ExternalSystemMapping\" where "
						+ " \"GROUP_ID\"= ? and \"SYSTEM_NAME\"= ?  and \"MAPPING_FLAG\"='0' and \"CLIENT\" = ?";
		String pstQuery = "";
		// If rule is for model or for equipment based on model's indicator then filter on model
		if(subjectType.equals("0") || (subjectType.equals("1") && isModelsIndicator)) {								
			pstQuery = pstQuery1 + "and \"MODEL_ID\"= ?";
		} else if(subjectType.equals("1")) {
			pstQuery = pstQuery1 + "and \"EQUIPMENT_ID\"= ?";
		}
		String pst = "";
		try {
			pst = jdbcTemplate.queryForObject(pstQuery,
					new Object[] {groupId, systemName, clientId, subjectId}, String.class);
		} catch (DataAccessException exception) {
			logger.error("Exception occured at RulesUtil.getPSTName{} ", exception.getMessage());
		}
		return pst;
	}

	public List<Equipment> getEquipmentDetails(String systemId, String equipmentId, String clientId) {
		List<Equipment> equipments = new ArrayList<>();
		try {
		String queryForRulesSubject =
				"Select * from \"_SYS_BIC\".\"sap.ain.views/EquipmentForRules\""
						+ "(PLACEHOLDER.\"$$iv_client$$\" => ?,"
						+ " PLACEHOLDER.\"$$iv_equipment_id$$\" => ?, PLACEHOLDER.\"$$iv_system_id$$\" => ?)";
		equipments = jdbcTemplate.query(queryForRulesSubject,
				new Object[] {clientId, equipmentId, systemId}, new Equipment());

		logger.debug("RuleService.getEquipmentListForAModel() returns equipments : "
				+ equipments.size());
		equipments.forEach(equ -> {
			logger.debug("Equipmentname: " + equ.getEquipmentId() + ", External Id: "
					+ equ.getEquipmentExternalId());
		});
		} catch (DataAccessException exception) {
			logger.error("Exception occured at RulesUtil.getEquipmentListForARule{} ", exception.getMessage());
		}
		return equipments;
	}

	public String getExternalIdModel(String modelId, String systemName, String clientId) {
		String modelQuery =
				"Select DISTINCT(\"THING_TYPE_NAME\") as \"THING_TYPE_NAME\"  from \"sap.ain.metaData::Configurations.ExternalSystemMapping\" "
						+ "where \"MODEL_ID\"= ? and \"SYSTEM_NAME\"= ? and \"MAPPING_FLAG\"='0' and \"CLIENT\" = ?";
		String model = "";
		try {
			model = jdbcTemplate.queryForObject(modelQuery,
					new Object[] {modelId, systemName, clientId}, String.class);
		} catch (DataAccessException exception) {
			logger.error("Exception occured at RulesUtil.getExternalIdModel{} ",
					exception.getMessage());
		}
		return model;
	}
}

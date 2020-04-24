/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sap.iot.ain.rules.implementation;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
//import java.sql.Timestamp;

import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.joda.time.DateTimeZone;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sap.iot.ain.template.dao.AlertTypeDao;
import com.sap.iot.ain.template.payload.Event;
import com.sap.iot.ain.core.AINObjectTypes;
import com.sap.iot.ain.indicator.v2.entities.IndicatorConfigurationThresholdEntity;
import com.sap.iot.ain.rules.inn365.Configuration;
import com.sap.iot.ain.rules.inn365.Email;
import com.sap.iot.ain.rules.inn365.EmailNotification;
import com.sap.iot.ain.rules.inn365.EmailRequestPayload;
import com.sap.dsc.ac.iotae.utils.ClientSetup;
import com.sap.dsc.ac.iotae.utils.IOTAeConstants;
import com.sap.iot.ain.notification.payload.AlertID;
import com.sap.iot.ain.notification.payload.AlertPayload;
import com.sap.iot.ain.notification.payload.NotificationDescription;
import com.sap.iot.ain.notification.payload.NotificationGET;
import com.sap.iot.ain.notification.payload.NotificationPOST;
import com.sap.iot.ain.notification.services.NotificationServices;
import com.sap.iot.ain.reuse.AINConstants;
import com.sap.iot.ain.reuse.GenericRowMapper;
import com.sap.iot.ain.reuse.Strings;
import com.sap.iot.ain.reuse.payload.AdminData;
import com.sap.iot.ain.reuse.utils.CustomScopeThreadLocal;
import com.sap.iot.ain.rules.evaluator.ExpressionEvaluator;
import com.sap.iot.ain.rules.models.AttributeValue;
import com.sap.iot.ain.rules.models.EmailActionParam;
import com.sap.iot.ain.rules.models.EmailAuthenticationInformation;
import com.sap.iot.ain.rules.models.EmailData;
import com.sap.iot.ain.rules.models.EmailRecipient;
import com.sap.iot.ain.rules.models.Equipment;
import com.sap.iot.ain.rules.models.ExecutableRuleJson;
import com.sap.iot.ain.rules.models.GetUsersForAnOrganization;
import com.sap.iot.ain.rules.models.Model;
import com.sap.iot.ain.rules.models.NotificationActionParam;
import com.sap.iot.ain.rules.models.Rule;
import com.sap.iot.ain.rules.models.RuleAction;
import com.sap.iot.ain.rules.models.RuleAggregation;
import com.sap.iot.ain.rules.models.RuleStep;
import com.sap.iot.ain.rules.models.RuleWithSteps;
import com.sap.iot.ain.rules.models.SMSActionParam;
import com.sap.iot.ain.rules.services.RuleService;
import com.sap.iot.ain.rules.uaa.UAAClientCredentialsTokenProvider;
import com.sap.iot.ain.rules.uaa.UAAServiceInfo;
import com.sap.iot.ain.rules.utils.Constant;
import com.sap.iot.ain.security.AINUserDetails;
import java.util.Arrays;

@Component
public class RuleExecutor {

	@Autowired
	private RuleService service;

	@PersistenceContext
	private EntityManager em;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private ClientSetup clientSetup;

	@Autowired
	private AlertTypeDao alertTypeDao;

	@Autowired
	private NotificationServices notificationServices;

	private static final Logger logger = LoggerFactory.getLogger(RuleExecutor.class);

	public RuleExecutor() {
	}

	public void execute(String ruleId, Instant fireTime, Instant previousFireTime, Equipment equipment, String executionId, String correlationId) {
		logger.debug("Rule execution begins. Rule Id is " + ruleId);
		String ruleExecutionStatus = Constant.EXECUTIONSTATUS.FAILURE.getValue();
		ExecutableRuleJson ruleJson = new ExecutableRuleJson();
		JSONObject ruleExecutionDetails = new JSONObject();
		try {
			ruleJson = prepareRuleJson(ruleId, fireTime, equipment);
			if (CustomScopeThreadLocal.get().get(AINConstants.RULES_REQUEST_AUD) == null) {
				logger.debug("Inside aud thread checks..");
				AINUserDetails ainUserDetails
				= new AINUserDetails(getUserBpIdOfRule(ruleJson.getRuleId()),
						ruleJson.getClient(), getTenantId(ruleJson.getClient()));
				CustomScopeThreadLocal.get().put(AINConstants.RULES_REQUEST_AUD, ainUserDetails);
				logger.debug("Successfully prepared ainuserDetails..");
			}
			
			logger.debug("Successfully prepared rule json. ");
			if (previousFireTime == null) {
				previousFireTime = fireTime;
			}
			Map<Integer, List<String>> iotAeUrls = processRuleSteps(ruleJson, fireTime, previousFireTime);
			logger.debug("Successfully processed rules stp. ");

			Map<String, Map<Integer, List<String>>> iotAEResponse = null;
			if (!iotAeUrls.isEmpty()) {
				iotAEResponse = callIoTAPIs(ruleJson, iotAeUrls, fireTime);
				logger.debug("Call to IoT APIs is successfull.");
			}
			ruleExecutionStatus = executeRuleJson(ruleJson, iotAEResponse, fireTime, previousFireTime, ruleExecutionDetails, correlationId);
			logger.error("execution of rule json is successful.");
		} catch (Exception e) {
			logger.error("Exeception while executing rules. " + e.getMessage());
			try {
				JSONObject failureReason = new JSONObject();
				failureReason.put("correlationId", correlationId);
				failureReason.put("executionStatus", ruleExecutionStatus);
				failureReason.put("reason", e.getMessage());
				ruleExecutionDetails.put(ruleExecutionStatus, failureReason);
			} catch (JSONException jsonException) {
				logger.error("Error while creating Json Object " + jsonException.getMessage());
			}
		} finally {
			
			service.updateRuleRun(ruleJson, ruleExecutionStatus, executionId, ruleJson.getInviteeClient());
			logger.debug("Persisted rule run.");
			if (!ruleExecutionStatus.equals(Constant.EXECUTIONSTATUS.SUCCESS.getValue())) {
				try {
					service.persistRuleExecutionLogs(ruleJson, ruleExecutionDetails.toString(4), executionId, ruleJson.getInviteeClient());
					logger.debug("Rule execution logs persisted.");
				} catch (JSONException jsonException) {
					logger.error("Error while persisting rule execution logs: {} " + jsonException.getMessage());
				}
			}
		}
		CustomScopeThreadLocal.unset();
	}

	private ExecutableRuleJson prepareRuleJson(String ruleId, Instant fireTime, Equipment equipment) throws Exception {
		logger.debug("prepareRuleJson:Enter");
		ExecutableRuleJson ruleJson = fetchRuleJsonForRuleId(ruleId);
		logger.debug("prepareRuleJson: fetching rule json is successful");
		ruleJson.setFireTime(fireTime);
		if (ruleJson.getRuleSubject().getSubjectType().compareToIgnoreCase("0") == 0) {
			resolveRuleJsonForModel(ruleJson, equipment);
			logger.debug("prepareRuleJson: rulejson resolved for model");
		} else if (ruleJson.getRuleSubject().getSubjectType().compareToIgnoreCase("1") == 0) {
			resolveRuleJsonForEquipment(ruleJson, equipment);
			logger.debug("prepareRuleJson :  rule json resolved for equipment");
		} else {
			logger.error("prepareRuleJson : Invalid subject type. Only 0 - Model and 1 - Equipment are supported.");
			throw new Exception("Invalid subject type. Only 0 - Model and 1 - Equipment are supported.");
		}
		return ruleJson;
	}

	private ExecutableRuleJson fetchRuleJsonForRuleId(String ruleId) {
		logger.debug("fetchRuleJsonForRuleId: Enter");
		ExecutableRuleJson ruleJson = new ExecutableRuleJson();
		RuleWithSteps ruleWithSteps = service.getRuleDetails(em, ruleId);
		String accType = getAccountType(ruleWithSteps.rule.getClient());
		logger.debug("CLient set for logs is "+ruleWithSteps.rule.getClient());
		ruleJson.setInviteeClient(ruleWithSteps.rule.getClient());
		if("3".equals(accType)) {
			logger.debug("Inside invitee check:AccountType is" + accType);
			ruleJson.setClient(getPremiumForInvitee(ruleWithSteps.rule.getClient()));
			
		}else {
			ruleJson.setClient(ruleWithSteps.rule.getClient());
		}
		ruleJson.setTenantId(ruleWithSteps.rule.getTenantSubDomain());
		ruleJson.setRuleId(ruleWithSteps.rule.getId());
		ruleJson.setName(ruleWithSteps.rule.getName());
		ruleJson.setPriority(ruleWithSteps.rule.getPriority());
		ruleJson.setIsEnabled(ruleWithSteps.rule.getIsEnabled());
		ruleJson.setDescription(ruleWithSteps.rule.getDescription());
		ruleJson.setRuleSubject(ruleWithSteps.ruleSubject);
		ruleJson.setRuleSteps(ruleWithSteps.ruleSteps);
		ruleJson.setRuleAggregations(ruleWithSteps.ruleAggregations);
		ruleJson.setRuleActions(ruleWithSteps.ruleActions);
		ruleJson.setRuleEvents(ruleWithSteps.ruleEvents);
		logger.debug("fetchRuleJsonForRuleId: success");
		return ruleJson;

	}

	private String getAccountType(String client) {
		
		String q = "select \"AccountType\"  from \"sap.ain.metaData::BusinessPartner.OrgAdditionalDetails\" "
				+ "where \"Client\" = ? and \"isActive\" = 1 ";
		String accountType = null;
		try {
			accountType = jdbcTemplate.queryForObject(q, new Object[] { client }, String.class);
		} catch (DataAccessException ex) {
			logger.error("Error while retrieving accoutn type for client, {}", ex);

		}
		logger.debug("Inside getAccountType():AccountType is" + accountType);
		return accountType;
		
	}

	private String getPremiumForInvitee(String inviteeClientId) {
		String q = "select \"BusinessPartner1\"  from \"sap.ain.metaData::BusinessPartner.BusinessPartnerRelationship\" "
				+ "where \"BusinessPartner2\" = ? and \"RelationshipType\" = 'BSC001' and \"IsMarkedForDeletion\" = 0";
		String premium = null;
		try {
			premium = jdbcTemplate.queryForObject(q, new Object[] { inviteeClientId }, String.class);
		} catch (DataAccessException ex) {
			logger.error("Error while retrieving premium id for invitee, {}", ex);

		}
		return premium;
	}
	private void resolveRuleJsonForModel(ExecutableRuleJson ruleJson, Equipment executorEquipment) throws Exception {
		logger.debug("resolveRuleJsonForModel : Enter");
		String externalpdmsSysThingId = getExternalSystemId(ruleJson.getClient(), IOTAeConstants.SYSTEM_NAME_THING);
		logger.debug("resolveRuleJsonForModel: External pdms thing id found");
		List<Equipment> equipments = getEquipmentListForRule(ruleJson.getRuleId(),
				externalpdmsSysThingId, ruleJson.getClient());
		logger.debug("resolveRuleJsonForModel : get equipmentlist for rule");

		if (equipments == null || equipments.isEmpty()) {
			logger.error("resolveRuleJsonForModel: No active equipments found for model");
			throw new Exception("No published equipments found for model.");
		} else {
			if (executorEquipment == null) {
				logger.error("resolveRuleJsonForModel: Equipment is not null");
				ruleJson.getRuleSubject().setEquipments(equipments);
			} else {
				List<Equipment> executorEquipments = new ArrayList<>();
				for (Equipment equipment : equipments) {
					if (equipment.getEquipmentId().equals(executorEquipment.getEquipmentId())) {
						executorEquipments.add(equipment);
					}
				}

				if (executorEquipments.isEmpty()) {
					logger.error("resolveRuleJsonForModel: Kindly check the Equipment supplied");
					throw new Exception("Invalid equipment passed for trigger based rule");
				} else {
					ruleJson.getRuleSubject().setEquipments(executorEquipments);
				}
			}

			Model model = new Model();
			model.setModelId(ruleJson.getRuleSubject().getSubjectID());
			String modelExternalId = getExternalIdForModel(ruleJson.getRuleSubject().getSubjectID(), IOTAeConstants.SYSTEM_NAME_PACKAGE, ruleJson.getClient());
			model.setModelExternalId(modelExternalId);
			ruleJson.getRuleSubject().setModel(model);
			logger.error("resolveRuleJsonForModel: Equipments fetched");

		}

	}

	private void resolveRuleJsonForEquipment(ExecutableRuleJson ruleJson, Equipment executorEquipment) throws Exception {
		logger.debug("resolveRuleJsonForEquipment: Enter");
		String externalpdmsSysThingId = getExternalSystemId(ruleJson.getClient(), IOTAeConstants.SYSTEM_NAME_THING);
		logger.debug("resolveRuleJsonForEquipment: external system id fetched");
		List<Equipment> equipments = getEquipmentDetails(externalpdmsSysThingId,
				ruleJson.getRuleSubject().getSubjectID(), ruleJson.getClient());

		if (equipments == null || equipments.isEmpty()) {
			logger.error("resolveRuleJsonForEquipment: No equipments found.");
			throw new Exception("Invalid equipment id.");
		} else {
			if (executorEquipment == null) {
				ruleJson.getRuleSubject().setEquipments(equipments);
			} else {
				List<Equipment> executorEquipments = new ArrayList<>();
				for (Equipment equipment : equipments) {
					if (equipment.getEquipmentId().equals(executorEquipment.getEquipmentId())) {
						executorEquipments.add(equipment);
					}
				}
				if (executorEquipments.isEmpty()) {
					logger.error("resolveRuleJsonForEquipment: Kindly check the Equipment supplied");
					throw new Exception("Invalid equipment passed for trigger based rule");
				} else {
					ruleJson.getRuleSubject().setEquipments(executorEquipments);
				}
			}
			logger.debug("resolveRuleJsonForEquipment: equipment is not null");

			Model model = new Model();
			model.setModelId(equipments.get(0).getModelId());
			String modelExternalId = getExternalIdForModel(equipments.get(0).getModelId(), IOTAeConstants.SYSTEM_NAME_PACKAGE, ruleJson.getClient());
			model.setModelExternalId(modelExternalId);
			ruleJson.getRuleSubject().setModel(model);
			logger.debug("resolveRuleJsonForEquipment: Model set");
		}

	}

	private String getPstForIndicatorGroup(ExecutableRuleJson ruleJson, String templateType, String indicatorGroup) throws Exception {
		logger.debug("getPstForIndicatorGroup: Enter");

		String subjectId = "";
		String pstQuery = "";
		String pst = null;
		String partialPSTQuery
		= "Select DISTINCT(\"PST_NAME\") as \"PST_NAME\" from \"sap.ain.metaData::Configurations.ExternalSystemMapping\" where "
				+ " \"GROUP_ID\"= ? and \"SYSTEM_NAME\"= ?  and \"MAPPING_FLAG\"='0' and \"CLIENT\" = ? and \"IS_VALID\" = 1 ";

		if (ruleJson.getRuleSubject().getSubjectType().equals("0")) {
			pstQuery = partialPSTQuery + "and \"MODEL_ID\"= ?";
			subjectId = ruleJson.getRuleSubject().getSubjectID();
		} else {
			if (templateType.equals("0")) {
				pstQuery = partialPSTQuery + "and \"MODEL_ID\"= ?";
				subjectId = ruleJson.getRuleSubject().getModel().getModelId();
			} else if (templateType.equals("1")) {
				pstQuery = partialPSTQuery + "and \"EQUIPMENT_ID\"= ?";
				subjectId = ruleJson.getRuleSubject().getSubjectID();
			}
		}
		try {
			pst = jdbcTemplate.queryForObject(pstQuery,
					new Object[]{indicatorGroup, IOTAeConstants.SYSTEM_NAME_PACKAGE, ruleJson.getClient(), subjectId}, String.class);

		} catch (Exception e) {
			logger.error("getPstForIndicatorGroup: Error while fetching external id of indicator group" + e.getMessage());
			throw new Exception("Error while fetching external id for Indicator Group");
		}

		if (pst == null) {
			logger.error("getPstForIndicatorGroup: Pst cannot be null");
			throw new Exception("Error while fetching external id for Indicator Group");
		}

		return pst;

	}

	private String getNamedPstForIndicatorGroup(ExecutableRuleJson ruleJson, String templateType, String indicatorGroup) throws Exception {
		logger.debug("getNamedPstForIndicatorGroup: Enter");
		String subjectId = "";
		String pstQuery = "";
		String pst = null;
		String partialPSTQuery
		= "Select DISTINCT(\"NAMED_PST_ID\") as \"PST_NAME\" from \"sap.ain.metaData::Configurations.ExternalSystemMapping\" where "
				+ " \"GROUP_ID\"= ? and \"SYSTEM_NAME\"= ?  and \"MAPPING_FLAG\"='0' and \"CLIENT\" = ? and  \"IS_VALID\" = 1 ";

		if (templateType == null || ruleJson.getRuleSubject().getSubjectType().equals("0")) {
			pstQuery = partialPSTQuery + "and \"MODEL_ID\"= ?";
			subjectId = ruleJson.getRuleSubject().getModel().getModelId();
		} else {
			if (templateType.equals("0")) {
				pstQuery = partialPSTQuery + "and \"MODEL_ID\"= ?";
				subjectId = ruleJson.getRuleSubject().getModel().getModelId();
			} else if (templateType.equals("1")) {
				pstQuery = partialPSTQuery + "and \"EQUIPMENT_ID\"= ?";
				subjectId = ruleJson.getRuleSubject().getSubjectID();
			}
		}
		try {
			pst = jdbcTemplate.queryForObject(pstQuery,
					new Object[]{indicatorGroup, IOTAeConstants.SYSTEM_NAME_PACKAGE, ruleJson.getClient(), subjectId}, String.class);

		} catch (Exception e) {
			logger.error("getNamedPstForIndicatorGroup: Error while fetching external id of indicator group");
			throw new Exception("Error while fetching external id for Indicator Group");
		}

		if (pst == null) {
			logger.error("getNamedPstForIndicatorGroup: pst cannot ber null");
			throw new Exception("Error while fetching external id for Indicator Group");
		}

		return pst;

	}

	private String getPropertyForIndicator(ExecutableRuleJson ruleJson, String templateType, String indicator) throws Exception {
		logger.debug("getPropertyForIndicator: Enter");

		String subjectId = "";
		String propertyQuery = "";
		String property = null;
		String partialPropertyQuery
		= "Select DISTINCT(\"PROPERTY_ID\") as \"PROPERTY_ID\" from "
				+ " \"sap.ain.metaData::Configurations.ExternalSystemMapping\" where  \"ATTRIBUTE_ID\" = ? and \"SYSTEM_NAME\"= ? and \"MAPPING_FLAG\"='0' "
				+ "and \"CLIENT\" =? and \"IS_VALID\" = 1 ";

		if (templateType == null || ruleJson.getRuleSubject().getSubjectType().equals("0")) {
			propertyQuery = partialPropertyQuery + "and \"MODEL_ID\"= ?";
			subjectId = ruleJson.getRuleSubject().getModel().getModelId();
		} else {
			if (templateType.equals("0")) {
				propertyQuery = partialPropertyQuery + "and \"MODEL_ID\"= ?";
				subjectId = ruleJson.getRuleSubject().getModel().getModelId();
			} else if (templateType.equals("1")) {
				propertyQuery = partialPropertyQuery + "and \"EQUIPMENT_ID\"= ?";
				subjectId = ruleJson.getRuleSubject().getSubjectID();
			}
		}

		try {
			property = jdbcTemplate.queryForObject(propertyQuery,
					new Object[]{indicator, IOTAeConstants.SYSTEM_NAME_PACKAGE, ruleJson.getClient(), subjectId}, String.class);

		} catch (Exception e) {
			logger.error("getPropertyForIndicator: Error while fetching external id for indicator.");
			throw new Exception("Error while fetching external id for Indicator.");
		}

		if (property == null) {
			logger.error("getPropertyForIndicator: property fetched is null.");
			throw new Exception("Error while fetching external id for Indicator.");
		}

		return property;

	}

	private Map<Integer, List<String>> processRuleSteps(ExecutableRuleJson ruleJson, Instant fireTime, Instant previousFireTime) throws Exception {
		logger.debug("processRuleSteps: Came inside process rules step");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		logger.error("processRuleSteps: Timestamp before conversion to String" + fireTime);
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		String fromTimeStamp = sdf.format(Date.from(previousFireTime));
		String toTimeStamp = sdf.format(Date.from(fireTime));
		logger.error("processRuleSteps: FromTimestamp after conversion to String" + toTimeStamp);
		logger.error("processRuleSteps: ToTimestamp after conversion to String" + fromTimeStamp);
		Map<Integer, List<String>> iotAEUrls = new HashMap<>();
		String condition = "and";
		String baseUrl = getExternalSystemURL(ruleJson.getClient(), IOTAeConstants.SYSTEM_NAME_INDICATORS);
		logger.debug("processRuleSteps: external system");

		String equipmentFilter = "";
		List<String> equipmentFilters = new ArrayList<>();

		for (Equipment equipment : ruleJson.getRuleSubject().getEquipments()) {
			if (equipmentFilter.length() <= 4000) {
				equipmentFilter = equipmentFilter + "id eq '" + equipment.getEquipmentExternalId() + "' or ";
			} else {
				equipmentFilter = equipmentFilter.substring(0, equipmentFilter.length() - 4);
				equipmentFilters.add(equipmentFilter);
				equipmentFilter = "";
			}

		}

		equipmentFilter = equipmentFilter.substring(0, equipmentFilter.length() - 4);
		equipmentFilters.add(equipmentFilter);

		logger.debug("processRuleSteps:equipment filter formed.");

		for (RuleStep ruleStep : ruleJson.getRuleSteps()) {
			logger.debug("Came inide rule step execution");
			if (ruleStep.getIsStepOperator() == 0) {
				logger.debug("processRuleSteps: Came insise step operator 0");
				if (ruleStep.getField1Type() != null && ruleStep.getField1Type().equalsIgnoreCase("NORMAL")) {
					logger.debug("processRuleSteps:Left hand is Norma Field");
					if (ruleStep.getField1IsIndicator() != null && ruleStep.getField1IsIndicator().equalsIgnoreCase("X")) {
						logger.debug("processRuleSteps: Logic for indicators");
						String pst = getPstForIndicatorGroup(ruleJson, ruleStep.getField1TemplateType(), ruleStep.getField1Group());
						String property = getPropertyForIndicator(ruleJson, ruleStep.getField1TemplateType(), ruleStep.getField1());
						ruleStep.setField1PST(pst);
						ruleStep.setField1Property(property);

					} else {
						logger.debug("processRuleSteps: Logic for attribute");
						if (ruleJson.getRuleSubject().getSubjectType().equals("0")) {
							String leftOperandValue = getAttributeValueForModel(ruleJson.getClient(),
									ruleJson.getRuleSubject().getModel().getModelId(), ruleStep.getField1Template(), ruleStep.getField1Group(),
									ruleStep.getField1(), ruleStep.getDataType());
							if (leftOperandValue == null) {
								logger.error("processRuleSteps: getAttributeValueForModel: Left hand value is null");
								throw new Exception("Value not found for attribute " + ruleStep.getField1());
							} else {
								logger.debug("processRuleSteps: field 1 value fetched");
								ruleStep.setField1Value(leftOperandValue);
							}
						} else {
							if (ruleStep.getField1TemplateType().equals("0")) {
								String leftOperandValue = getAttributeValueForModel(ruleJson.getClient(),
										ruleJson.getRuleSubject().getModel().getModelId(), ruleStep.getField1Template(), ruleStep.getField1Group(),
										ruleStep.getField1(), ruleStep.getDataType());
								if (leftOperandValue == null) {
									logger.error("processRuleSteps: getAttributeValueForModel: Left hand value is null");
									throw new Exception("Value not found for attribute " + ruleStep.getField1());
								} else {
									ruleStep.setField1Value(leftOperandValue);
								}
							} else {
								String leftOperandValue = getAttributeValueForEquipment(ruleJson.getClient(),
										ruleJson.getRuleSubject().getSubjectID(), ruleStep.getField1Template(), ruleStep.getField1Group(),
										ruleStep.getField1(), ruleStep.getDataType());
								if (leftOperandValue == null) {
									logger.error("processRuleSteps: getAttributeValueForEquipment: Left hand value is null");
									throw new Exception("value not found for attribute " + ruleStep.getField1());
								} else {
									ruleStep.setField1Value(leftOperandValue);
								}
							}
						}
					}
				} else if (ruleStep.getField1Type() != null && ruleStep.getField1Type().equalsIgnoreCase("AGG")) {
					logger.debug("processRuleSteps: Left hand side aggregation logic");
					Optional<RuleAggregation> optionalRuleaggregation = ruleJson.getRuleAggregations().stream().filter(aggregation -> aggregation.getName().equals(ruleStep.getField1())).findFirst();
					if (optionalRuleaggregation.isPresent()) {
						String pst = getPstForIndicatorGroup(ruleJson, optionalRuleaggregation.get().getFieldTemplateType(), optionalRuleaggregation.get().getFieldGroup());
						String property = getPropertyForIndicator(ruleJson, optionalRuleaggregation.get().getFieldTemplateType(), optionalRuleaggregation.get().getFieldID());
						optionalRuleaggregation.get().setFieldProperty(property);
						optionalRuleaggregation.get().setFieldPST(pst);
					}
				} else {
					logger.error("processRuleSteps: Invalid field type. Only normal and aggrgation");
					throw new Exception("Invalid field type. Only Normal and Aggregation supported for left operand.");
				}

				//Logic for right operand 
				if (ruleStep.getField2Type() != null && ruleStep.getField2Type().equalsIgnoreCase("NORMAL")) {
					logger.debug("processRuleSteps: Right hand side logic for field");
					if (ruleStep.getField2IsIndicator().equalsIgnoreCase("X")) {
						logger.error("processRuleSteps:Right hand side logic for inidicators. Not supported. ");
						throw new Exception("Indicator is not supported for right hand side operator.");
					} else {
						logger.debug("processRuleSteps: Right hand size. Logic for attribute.");
						if (ruleJson.getRuleSubject().getSubjectType().equals("0")) {
							String rightOperandValue = getAttributeValueForModel(ruleJson.getClient(),
									ruleJson.getRuleSubject().getModel().getModelId(), ruleStep.getField2Template(), ruleStep.getField2Group(),
									ruleStep.getField2(), ruleStep.getDataType());
							if (rightOperandValue == null) {
								throw new Exception("Value not found for attribute " + ruleStep.getField2());
							} else {
								ruleStep.setField2Value(rightOperandValue);
							}
						} else {
							if (ruleStep.getField2TemplateType().equals("0")) {
								String rightOperandValue = getAttributeValueForModel(ruleJson.getClient(),
										ruleJson.getRuleSubject().getModel().getModelId(), ruleStep.getField2Template(), ruleStep.getField2Group(),
										ruleStep.getField2(), ruleStep.getDataType());
								if (rightOperandValue == null) {
									throw new Exception("Value not found for attribute " + ruleStep.getField2());
								} else {
									ruleStep.setField2Value(rightOperandValue);
								}
							} else {
								String rightOperandValue = getAttributeValueForEquipment(ruleJson.getClient(),
										ruleJson.getRuleSubject().getSubjectID(), ruleStep.getField2Template(), ruleStep.getField2Group(),
										ruleStep.getField2(), ruleStep.getDataType());
								if (rightOperandValue == null) {
									throw new Exception("Value not found for attribute " + ruleStep.getField2());
								} else {
									ruleStep.setField2Value(rightOperandValue);
								}
							}
						}
					}
				} else if (ruleStep.getField2Type() != null && ruleStep.getField2Type().equalsIgnoreCase("CONSTANT")) {
					if (ruleStep.getField2() != null) {
						logger.debug("processRuleSteps: right hand side constant value.");
						ruleStep.setField2Value(ruleStep.getField2());
					} else {
						logger.error("processRuleSteps: Constant value not maintained.");
						throw new Exception("Constant value not maintained.");
					}
				} else if (ruleStep.getField2Type() != null && ruleStep.getField2Type().equalsIgnoreCase("THRESHOLD")) {
					logger.debug("processRuleSteps: Logic for threshold.");
					String rightOperandValue = ruleStep.getField2();
					String[] indicatorInstanceDetails = rightOperandValue.split(",");
					String indicatorInstanceId = indicatorInstanceDetails[0];
					String displayOrder = indicatorInstanceDetails[1];
					String rangeFrom = null;
					String rangeTo = null;
					TypedQuery<IndicatorConfigurationThresholdEntity> thresholdForIndicatorQuery
					= em.createQuery("Select iite from  IndicatorConfigurationThresholdEntity iite where iite.configId=:configId and iite.displayOrder=:displayOrder", IndicatorConfigurationThresholdEntity.class);
					thresholdForIndicatorQuery.setParameter("configId", indicatorInstanceId);
					thresholdForIndicatorQuery.setParameter("displayOrder", Integer.parseInt(displayOrder));
					List<IndicatorConfigurationThresholdEntity> indicatorInstanceThresholdEntityList = thresholdForIndicatorQuery.getResultList();
					if (indicatorInstanceThresholdEntityList != null && !indicatorInstanceThresholdEntityList.isEmpty()) {
						IndicatorConfigurationThresholdEntity entity = indicatorInstanceThresholdEntityList.get(0);
						//AC stores from as maximum value and to as minimum value
						if(entity.getRangeFrom() != null)
						 rangeTo = entity.getRangeFrom().toString();
						if(entity.getRangeTo() != null)
						 rangeFrom = entity.getRangeTo().toString();
						if (ruleStep.getOperator().equalsIgnoreCase(Constant.IN_RANGE)) {
							ruleStep.setField2FromRange(rangeFrom);
							ruleStep.setField2ToRange(rangeTo);
							condition = "and";
						} else if (ruleStep.getOperator().equalsIgnoreCase(Constant.NOT_IN_RANGE)) {
							ruleStep.setField2FromRange(rangeTo);
							ruleStep.setField2ToRange(rangeFrom);
							condition = "or";
						}

					}
				} else {
					//Throw exception
					logger.error("Only attribute, constant, threshold are supported.");
					throw new Exception("Only Attribute,Constant, Threshold supported on right hand");
				}

				//At this stage only left hand side and right hand side having Attribute is evaluated. So, evaluate it's result.
				//For indicators construct URL
				if (ruleStep.getField1Type() != null && ruleStep.getField1Type().equalsIgnoreCase("NORMAL")) {
					if (ruleStep.getField1IsIndicator() != null && ruleStep.getField1IsIndicator().equalsIgnoreCase("X")) {

						//Construct IoTAE URL for Normal fields.
						if (ruleStep.getField2Type() != null && ruleStep.getField2Type().equalsIgnoreCase("THRESHOLD")) {
							//Logic for Threshold url.
							if (ruleStep.getField2FromRange() != null && ruleStep.getField2ToRange() != null) {
								logger.debug("processRuleSteps: Constructing threshold url for from and to range");
								iotAEUrls.put(ruleStep.getSequenceNo(), formThrehsoldMeasurementUrl(baseUrl, ruleStep.getField1PST(), fromTimeStamp, toTimeStamp, ruleStep.getField1Property(), ruleStep.getField2FromRange(), ruleStep.getField2ToRange(), equipmentFilters, condition,ruleStep.getDataType()));
							} else if (ruleStep.getField2FromRange() != null && ruleStep.getField2ToRange() == null) {
								////NO upper range
								logger.debug("processRuleSteps: Constructing threshold url for from and no to range");
								iotAEUrls.put(ruleStep.getSequenceNo(), formNoUpperLimitThrehsoldMeasurementUrl(baseUrl, ruleStep.getField1PST(), fromTimeStamp, toTimeStamp, ruleStep.getField1Property(), ruleStep.getField2FromRange(), equipmentFilters, condition,ruleStep.getDataType()));
							} else if (ruleStep.getField2FromRange() == null && ruleStep.getField2ToRange() != null) {
								//No lower range
								logger.debug("processRuleSteps: Constructing threshold url for to range and no from range");
								iotAEUrls.put(ruleStep.getSequenceNo(), formNoLowerLimitThrehsoldMeasurementUrl(baseUrl, ruleStep.getField1PST(), fromTimeStamp, toTimeStamp, ruleStep.getField1Property(), ruleStep.getField2ToRange(), equipmentFilters, condition,ruleStep.getDataType()));
							}else {
								logger.debug("Invalid range.");
								throw new Exception("Invalid range for aggregation. Both cannot be null");
							}

						} else {
							String operator = null;

							if (ruleStep.getOperator().equalsIgnoreCase(Constant.EQUAL)) {
								operator = "eq";
							} else if (ruleStep.getOperator().equalsIgnoreCase(Constant.NOTEQUAL)) {
								operator = "ne";
							} else if (ruleStep.getOperator().equalsIgnoreCase(Constant.GREATER)) {
								operator = "gt";
							} else if (ruleStep.getOperator().equalsIgnoreCase(Constant.GREATEROREQUAL)) {
								operator = "ge";
							} else if (ruleStep.getOperator().equalsIgnoreCase(Constant.LESSER)) {
								operator = "lt";
							} else if (ruleStep.getOperator().equalsIgnoreCase(Constant.LESSEROREQUAL)) {
								operator = "le";
							}

							if (operator == null) {
								//Throw exception here.
								logger.error("processRuleSteps:Invalid operator.");
								throw new Exception("Invalid operator. Operator not supported or invalid operator passed.");
							}
							logger.debug("processRuleSteps: Forming simple url");
							iotAEUrls.put(ruleStep.getSequenceNo(), formSimpleMeasurementUrl(baseUrl, ruleStep.getField1PST(), fromTimeStamp, toTimeStamp, ruleStep.getField1Property(), operator, ruleStep.getField2Value(), equipmentFilters,ruleStep.getDataType()));
						}
					} else {
						logger.debug("processRuleSteps: Logic for attribut evaluation");
						int response = 0;

						if (ruleStep.getDataType() != null && ruleStep.getDataType().equalsIgnoreCase(Constant.STRING)) {
							ExpressionEvaluator<String> expressionEvaluator = new ExpressionEvaluator<>();
							response = expressionEvaluator.compare(ruleStep.getField1Value(), ruleStep.getField2Value());
							logger.debug("processRuleSteps: String comparision true.");
						} else if (ruleStep.getDataType() != null && (ruleStep.getDataType().equalsIgnoreCase(Constant.NUMBER)
								|| ruleStep.getDataType().equalsIgnoreCase(Constant.NUMERIC) || ruleStep.getDataType().equalsIgnoreCase(Constant.NUMERICFLEXIBLE))) {
							ExpressionEvaluator<Double> expressionEvaluator = new ExpressionEvaluator<>();
							response = expressionEvaluator.compare(Double.parseDouble(ruleStep.getField1Value()), Double.parseDouble(ruleStep.getField2Value()));
							logger.debug("processRuleSteps: Numeric comparision");
						} else if (ruleStep.getDataType() != null && ruleStep.getDataType().equalsIgnoreCase(Constant.DATE)) {
							ExpressionEvaluator<Date> expressionEvaluator = new ExpressionEvaluator<>();
							DateFormat format = new SimpleDateFormat("MMMM d, yyyy");
							format.setTimeZone(TimeZone.getTimeZone("UTC"));
							response = expressionEvaluator.compare(new Date(new Long(ruleStep.getField1Value())), format.parse(ruleStep.getField2Value()));
							logger.debug("processRuleSteps: Date comparision");
						} else if(ruleStep.getDataType() != null && ruleStep.getDataType().equalsIgnoreCase(Constant.BOOLEAN)){
							ExpressionEvaluator<String> expressionEvaluator = new ExpressionEvaluator<>();
							response = expressionEvaluator.compare(ruleStep.getField1Value().toLowerCase(), ruleStep.getField2Value().toLowerCase());
							logger.debug("processRuleSteps: Boolean string value comparision");
						}else {
							//Throw exception.
							logger.error("processRuleSteps: Unsupported data type");
							throw new Exception("Unsupported data type for comaprision");
						}

						//Now set status at rulestep based on result.
						if (response > 0) {
							logger.debug("processRuleSteps: Response is greater than 0");
							if (ruleStep.getOperator().equalsIgnoreCase(Constant.GREATER) || ruleStep.getOperator().equalsIgnoreCase(Constant.GREATEROREQUAL) || ruleStep.getOperator().equalsIgnoreCase(Constant.NOTEQUAL)) {
								ruleStep.setExecutionStatus(true);
							} else {
								ruleStep.setExecutionStatus(false);
							}
						} else if (response == 0) {
							logger.debug("processRuleSteps: Response is equal to 0");
							if (ruleStep.getOperator().equalsIgnoreCase(Constant.EQUAL)
									|| ruleStep.getOperator().equalsIgnoreCase(Constant.GREATEROREQUAL)
									|| ruleStep.getOperator().equalsIgnoreCase(Constant.LESSEROREQUAL)) {
								ruleStep.setExecutionStatus(true);

							} else {
								ruleStep.setExecutionStatus(false);
							}
						} else if (response < 0) {
							logger.debug("processRuleSteps: Response is less than 0");
							if (ruleStep.getOperator().equalsIgnoreCase(Constant.LESSER)
									|| ruleStep.getOperator().equalsIgnoreCase(Constant.LESSEROREQUAL)
									|| ruleStep.getOperator().equalsIgnoreCase(Constant.NOTEQUAL)) {
								ruleStep.setExecutionStatus(true);
							} else {
								ruleStep.setExecutionStatus(false);
							}

						}
					}
				} else if (ruleStep.getField1Type() != null && ruleStep.getField1Type().equalsIgnoreCase("AGG")) {
					//Construct IotAE Url for aggregation fields.
					logger.debug("processRuleSteps: Constructing url for begins. Loggic for aggregation.");
					Optional<RuleAggregation> optionalRuleAggregation = ruleJson.getRuleAggregations().stream().filter(aggregation -> aggregation.getName().equals(ruleStep.getField1())).findFirst();
					if (optionalRuleAggregation.isPresent()) {
						logger.debug("processRuleSteps: Rule aggregation present");

						RuleAggregation ruleAggregation = optionalRuleAggregation.get();
						String startWithoutMilli = null;
						if (ruleAggregation.getTimeUnit().equalsIgnoreCase("S")) {
							startWithoutMilli = sdf.format(Date.from(fireTime.minus(Duration.ofSeconds(Integer.parseInt(ruleAggregation.getTimeFilter())))));
						} else if (ruleAggregation.getTimeUnit().equalsIgnoreCase("M")) {
							startWithoutMilli = sdf.format(Date.from(fireTime.minus(Duration.ofMinutes(Integer.parseInt(ruleAggregation.getTimeFilter())))));
						} else if (ruleAggregation.getTimeUnit().equalsIgnoreCase("H")) {
							startWithoutMilli = sdf.format(Date.from(fireTime.minus(Duration.ofHours(Integer.parseInt(ruleAggregation.getTimeFilter())))));
						} else if (ruleAggregation.getTimeUnit().equalsIgnoreCase("D")) {
							startWithoutMilli = sdf.format(Date.from(fireTime.minus(Duration.ofDays(Integer.parseInt(ruleAggregation.getTimeFilter())))));
						}
						String endWithoutMilli = sdf.format(Date.from(fireTime));
						String operator = null;
						if (ruleStep.getOperator().equalsIgnoreCase(Constant.EQUAL)) {
							operator = "eq";
						} else if (ruleStep.getOperator().equalsIgnoreCase(Constant.NOTEQUAL)) {
							operator = "ne";
						} else if (ruleStep.getOperator().equalsIgnoreCase(Constant.GREATER)) {
							operator = "gt";
						} else if (ruleStep.getOperator().equalsIgnoreCase(Constant.GREATEROREQUAL)) {
							operator = "ge";
						} else if (ruleStep.getOperator().equalsIgnoreCase(Constant.LESSER)) {
							operator = "lt";
						} else if (ruleStep.getOperator().equalsIgnoreCase(Constant.LESSEROREQUAL)) {
							operator = "le";
						}

						String property = ruleAggregation.getFieldProperty() + "_" + ruleAggregation.getFunctionName();

						if (operator == null) {
							throw new Exception("Invalid operator. Operator not supported or invalid operator passed.");
						}
						//Construct IoTAE URL for Normal fields.
						if (ruleStep.getField2Type() != null && ruleStep.getField2Type().equalsIgnoreCase("THRESHOLD")) {
							if (ruleStep.getField2FromRange() != null && ruleStep.getField2ToRange() != null) {
								iotAEUrls.put(ruleStep.getSequenceNo(), formThresholdAggregationUrl(baseUrl, ruleAggregation.getFieldPST(), startWithoutMilli, endWithoutMilli, property, ruleStep.getField2FromRange(), ruleStep.getField2ToRange(), equipmentFilters,ruleStep.getDataType()));
							} else if (ruleStep.getField2FromRange() != null && ruleStep.getField2ToRange() == null) {
								////NO upper range
								iotAEUrls.put(ruleStep.getSequenceNo(), formNoUpperLimitThresholdAggregationUrl(baseUrl, ruleAggregation.getFieldPST(), startWithoutMilli, endWithoutMilli, property, ruleStep.getField2FromRange(), equipmentFilters,ruleStep.getDataType()));
							} else if (ruleStep.getField2FromRange() == null && ruleStep.getField2ToRange() != null) {
								//No lower range
								iotAEUrls.put(ruleStep.getSequenceNo(), formNoLowerLimitThresholdAggregationUrl(baseUrl, ruleAggregation.getFieldPST(), startWithoutMilli, endWithoutMilli, property, ruleStep.getField2ToRange(), equipmentFilters,ruleStep.getDataType()));
							} else {
								throw new Exception("Invalid range for aggregation. Both cannot be null");
							}
							//Logic for Threshold url.

						} else {
							// String aggregationProperty = property+"_" + ruleAggregation.getFunctionName().toUpperCase();
							iotAEUrls.put(ruleStep.getSequenceNo(), formSimpleAggregationUrl(baseUrl, ruleAggregation.getFieldPST(), startWithoutMilli, endWithoutMilli, property, operator, ruleStep.getField2Value(), equipmentFilters,ruleStep.getDataType()));
						}
					}
				}
			}

		}
		return iotAEUrls;
	}

	private List<Equipment> getEquipmentListForRule(String ruleId, String systemId,
			String clientId) throws Exception {
		logger.debug("getEquipmentListForRule: Enter");
		List<Equipment> equipments = new ArrayList<>();
		try {
			String queryForRulesSubject
			= "Select * from \"_SYS_BIC\".\"sap.ain.views/EquipmentsForModel\""
					+ "(PLACEHOLDER.\"$$iv_client$$\" => ?,"
					+ " PLACEHOLDER.\"$$iv_rule_id$$\" => ?, PLACEHOLDER.\"$$iv_system_id$$\" => ?)";
			equipments = jdbcTemplate.query(queryForRulesSubject,
					new Object[]{clientId, ruleId, systemId}, new Equipment());
			equipments.forEach(equ -> {
			});
		} catch (DataAccessException exception) {
			logger.error("getEquipmentListForRule: Data access exception " + exception.getMessage());
			throw new Exception("Failed to get equipment list for rule.");
		}
		logger.debug("getEquipmentListForRule: Returning equipments");
		return equipments;
	}

	private String getExternalIdForModel(String modelId, String systemName, String clientId) throws Exception {
		logger.debug("getExternalIdForModel: Enter");
		String modelQuery
		= "Select DISTINCT(\"THING_TYPE_NAME\") as \"THING_TYPE_NAME\"  from \"sap.ain.metaData::Configurations.ExternalSystemMapping\" "
				+ "where \"MODEL_ID\"= ? and \"SYSTEM_NAME\"= ? and \"MAPPING_FLAG\"='0' and \"CLIENT\" = ? and \"IS_VALID\" = 1 ";
		String model = "";
		try {
			model = jdbcTemplate.queryForObject(modelQuery,
					new Object[]{modelId, systemName, clientId}, String.class);
		} catch (DataAccessException exception) {
			logger.error("getExternalIdForModel: Data access exception " + exception.getMessage());
			throw new Exception("Failed to fetch external id for model.");
		}
		logger.debug("getExternalIdForModel: Returning model");
		return model;
	}

	private String getExternalSystemId(String clientId, String systemName) throws Exception {
		logger.debug("getExternalSystemId: Enter");
		String query
		= "SELECT top 1 \"ID\" FROM \"_SYS_BIC\".\"sap.ain.views/ExternalSystemsList\" (PLACEHOLDER.\"$$iv_client$$\" => ? ,PLACEHOLDER.\"$$iv_lang$$\"=>?)"
				+ " where \"SystemStatusDescription\" = 'Active' and \"SystemType\" = 'PdMS' and \"SystemName\" = ?";

		String extSysId = "";
		try {
			extSysId = jdbcTemplate.queryForObject(query, new Object[]{clientId, "en", systemName},
					String.class);
		} catch (DataAccessException exception) {
			logger.error("getExternalSystemId: Data access exception " + exception.getMessage());
			throw new Exception("Failed to fetch external system Id for system " + systemName);
		}

		logger.debug("getExternalSystemId: Returning external system id");
		return extSysId;
	}

	private String getExternalSystemURL(String clientId, String systemName) throws Exception {
		logger.debug("getExternalSystemURL: Enter");
		String query
		= "SELECT top 1 \"URL1\" FROM \"_SYS_BIC\".\"sap.ain.views/ExternalSystemsList\" (PLACEHOLDER.\"$$iv_client$$\" => ? ,PLACEHOLDER.\"$$iv_lang$$\"=>?)"
				+ " where \"SystemStatusDescription\" = 'Active' and \"SystemType\" = 'PdMS' and \"SystemName\" = ?";

		String extSysId = "";
		try {
			extSysId = jdbcTemplate.queryForObject(query, new Object[]{clientId, "en", systemName},
					String.class);
		} catch (DataAccessException exception) {
			logger.error("getExternalSystemURL: Data access exception " + exception.getMessage());
			throw new Exception("Failed to fetch external system url for system " + systemName);
		}
		logger.debug("getExternalSystemURL: returning external system id");

		return extSysId;
	}

	private String getAttributeValueForModel(String client, String modelId, String template, String attributeGroup, String attribute, String dataType) {
		logger.debug("getAttributeValueForModel: Enter");
		String query = "Select \"ObjectValue\".\"Client\", \"ObjectValue\".\"ID\", \"ObjectValue\".\"PropertyID\", \"ObjectValue\".\"BooleanValue\", "
				+ " \"ObjectValue\".\"DateValue\", \"ObjectValue\".\"StringValue\", \"ObjectValue\".\"Norm_1\" from \"AIN_DEV\".\"sap.ain.metaData::Object.Value\" "
				+ " as \"ObjectValue\" where \"ObjectValue\".\"ID\"= ? and "
				+ " \"ObjectValue\".\"PropertyID\"= ? and \"ObjectValue\".\"Type\"= ? "
				+ " and \"ObjectValue\".\"PSTID\" = ? and \"ObjectValue\".\"CategoryID\" = ?"
				+ " and \"ObjectValue\".\"Version\"=  (SELECT \"versionheader\".\"ModelVersion\" FROM \"sap.ain.metaData::Model.Header\" as "
				+ "  \"versionheader\" WHERE \"versionheader\".\"ID\" = \"ObjectValue\".\"ID\" and "
				+ " \"versionheader\".\"isMarkedForDeletion\" = '0' and \"versionheader\".\"Status\"='2') ";
		Object[] params = new Object[]{modelId, attribute, AINObjectTypes.MOD.toString(),attributeGroup, template};

		List<AttributeValue> attributeValues = jdbcTemplate.query(query,
				params,
				new AttributeValue());

		if (attributeValues == null || attributeValues.isEmpty()) {
			logger.error("getAttributeValueForModel: attribute values are empty.");
			return null;
		}
		AttributeValue attributeValue = attributeValues.get(0);

		if (dataType.equalsIgnoreCase("string")) {
			return attributeValue.getStringValue();
		} else if ((dataType.equalsIgnoreCase("numeric")) || (dataType.equalsIgnoreCase("numericflexible"))) {
			return attributeValue.getNumericValue();
		} else if (dataType.equalsIgnoreCase("date")) {
			if (attributeValue.getDateValue() != null) {
				return new Long(attributeValue.getDateValue().getTime()).toString();
			}
		}else if(dataType.equalsIgnoreCase(Constant.BOOLEAN)){
			if(attributeValue.getBooleanValue().equals("1")){
				return new Boolean(true).toString();
			}
			return new Boolean(false).toString();
		}

		return null;
	}

	private String getAttributeValueForEquipment(String client, String equipmentId, String template, String group, String attribute, String dataType) {
		logger.debug("getAttributeValueForEquipment: Enter");
		String query = "Select \"ObjectValue\".\"Client\", \"ObjectValue\".\"ID\", \"ObjectValue\".\"PropertyID\", \"ObjectValue\".\"BooleanValue\", "
				+ " \"ObjectValue\".\"DateValue\", \"ObjectValue\".\"StringValue\", \"ObjectValue\".\"Norm_1\" from \"AIN_DEV\".\"sap.ain.metaData::Object.Value\" "
				+ " as \"ObjectValue\" where \"ObjectValue\".\"ID\"= ? and "
				+ " \"ObjectValue\".\"PropertyID\"= ? and \"ObjectValue\".\"Type\"= ? "
				+ " and \"ObjectValue\".\"PSTID\" = ? and \"ObjectValue\".\"CategoryID\" = ?"
				+ " and \"ObjectValue\".\"Version\"=  (SELECT \"versionheader\".\"EquipmentVersion\" FROM \"sap.ain.metaData::Equipment.Header\" as "
				+ "  \"versionheader\" WHERE \"versionheader\".\"ID\" = \"ObjectValue\".\"ID\" and "
				+ " \"versionheader\".\"isMarkedForDeletion\" = '0' and \"versionheader\".\"Status\"='2') ";
		Object[] params = new Object[]{equipmentId, attribute, AINObjectTypes.EQU.toString(), group, template};

		List<AttributeValue> attributeValues = jdbcTemplate.query(query,
				params,
				new AttributeValue());

		if (attributeValues == null || attributeValues.isEmpty()) {
			logger.error("getAttributeValueForEquipment: Attribute values are null");
			return null;
		}
		AttributeValue attributeValue = attributeValues.get(0);

		if (dataType.equalsIgnoreCase("string")) {
			return attributeValue.getStringValue();
		} else if ((dataType.equalsIgnoreCase("numeric")) || (dataType.equalsIgnoreCase("numericflexible"))) {
			return attributeValue.getNumericValue();
		} else if (dataType.equalsIgnoreCase("date")) {
			if (attributeValue.getDateValue() != null) {
				return new Long(attributeValue.getDateValue().getTime()).toString();
			}
		}else if(dataType.equalsIgnoreCase(Constant.BOOLEAN)){
			if(attributeValue.getBooleanValue().equals("1")){
				return new Boolean(true).toString();
			}
			return new Boolean(false).toString();
		}

		return null;
	}

	private List<Equipment> getEquipmentDetails(String systemId, String equipmentId, String clientId) throws Exception {
		logger.debug("getEquipmentDetails: Enter");
		List<Equipment> equipments = new ArrayList<>();
		try {
			String queryForRulesSubject
			= "Select * from \"_SYS_BIC\".\"sap.ain.views/EquipmentForRules\""
					+ "(PLACEHOLDER.\"$$iv_client$$\" => ?,"
					+ " PLACEHOLDER.\"$$iv_equipment_id$$\" => ?, PLACEHOLDER.\"$$iv_system_id$$\" => ?)";
			equipments = jdbcTemplate.query(queryForRulesSubject,
					new Object[]{clientId, equipmentId, systemId}, new Equipment());

		} catch (DataAccessException exception) {
			logger.error("getEquipmentDetails: exception while getting equipment details. " + exception.getMessage());
			throw new Exception("Failed to fetch equipments for the rule ");
		}
		return equipments;
	}

	/**
	 *  This method build IOTAE API URL based on the indicator and value date created on rule expression ,
	 *  In case of number flex which is double type of data f need to appended and boolean converted to lover case ("True" or true is same  )
	 * @param url
	 * @param pst
	 * @param fromtimestamp
	 * @param toTimestamp
	 * @param property
	 * @param operator
	 * @param value
	 * @param equipmentFilters
	 * @param dataType
	 * @return
	 */
	private List<String> formSimpleMeasurementUrl(String url, String pst, String fromtimestamp, String toTimestamp, String property, String operator, String value, List<String> equipmentFilters,String dataType ) {
		List<String> iotAEUrls = new ArrayList<>();
		if(dataType.equals(Constant.NUMERICFLEXIBLE)){
			value +="f";
		}

		if(dataType.equals(Constant.BOOLEAN)){
			value = value.toLowerCase();
		}
		//for (String equipmentFilter : equipmentFilters) {
		String baseurl = url
				+ "/"
				+ pst
				+ "/measurements?$filter=";

		baseurl = baseurl + "time ge datetime"
				+ "'"
				+ fromtimestamp
				+ "'" // From time stamp
				+ " and time lt datetime"
				+ "'"
				+ toTimestamp
				+ "'" //To time stamp
				+ " and  "
				+ property
				+ " "
				+ operator
				+ " "
				+ value
				+ "&$select=id, time&$format=json";
		baseurl = baseurl.replaceAll(" ", "%20");
		iotAEUrls.add(baseurl);

		logger.debug("formSimpleMeasurementUrl: URL is " + baseurl);

		// }
		return iotAEUrls;
	}

	private List<String> formThrehsoldMeasurementUrl(String url, String pst, String fromtimestamp, String toTimestamp, String property, String rangeFrom, String rangeTo, List<String> equipmentFilters, String condition,String dataType) {
		List<String> iotAEUrls = new ArrayList<>();
		if(dataType.equals(Constant.NUMERICFLEXIBLE)){
			rangeFrom +="f";
			rangeTo +="f";
		}
		//for (String equipmentFilter : equipmentFilters) {
		String baseurl = url
				+ "/"
				+ pst
				+ "/measurements?$filter=";

		baseurl = baseurl + "time ge datetime"
				+ "'"
				+ fromtimestamp
				+ "'" // From time stamp
				+ " and time lt datetime"
				+ "'"
				+ toTimestamp
				+ "'" //To time stamp
				
				+ " and "
				
				+ property
				+ " gt "
				+ rangeFrom
				+ " "
				+ condition
				+ " "
				+ property
				+ " le "
				+ rangeTo
				+ "&$select=id, time&$format=json";
		baseurl = baseurl.replaceAll(" ", "%20");
		iotAEUrls.add(baseurl);
		logger.debug("formThrehsoldMeasurementUrl: URL is " + baseurl);
		// }

		return iotAEUrls;
	}

	private List<String> formNoUpperLimitThrehsoldMeasurementUrl(String url, String pst, String fromtimestamp, String toTimestamp, String property, String rangeFrom, List<String> equipmentFilters, String condition, String dataType) {
		List<String> iotAEUrls = new ArrayList<>();
		if(dataType.equals(Constant.NUMERICFLEXIBLE)){
			rangeFrom +="f";
		}
		// for (String equipmentFilter : equipmentFilters) {
		String baseurl = url
				+ "/"
				+ pst
				+ "/measurements?$filter=";

		baseurl = baseurl + "time ge datetime"
				+ "'"
				+ fromtimestamp
				+ "'" // From time stamp
				+ " and time lt datetime"
				+ "'"
				+ toTimestamp
				+ "'" //To time stamp
				+ " "
				+ "and"
				+ " "
				+ property
				+ " gt "
				+ rangeFrom
				+ "&$select=id, time&$format=json";
		baseurl = baseurl.replaceAll(" ", "%20");
		iotAEUrls.add(baseurl);
		logger.debug("formNoUpperLimitThrehsoldMeasurementUrl: URL is " + baseurl);

		//  }
		return iotAEUrls;
	}

	private List<String> formNoLowerLimitThrehsoldMeasurementUrl(String url, String pst, String fromtimestamp, String toTimestamp, String property, String rangeTo, List<String> equipmentFilters, String condition,String dataType) {
		List<String> iotAEUrls = new ArrayList<>();
		if(dataType.equals(Constant.NUMERICFLEXIBLE)){
			rangeTo +="f";
		}
		// for (String equipmentFilter : equipmentFilters) {
		String baseurl = url
				+ "/"
				+ pst
				+ "/measurements?$filter=";

		baseurl = baseurl + "time ge datetime"
				+ "'"
				+ fromtimestamp
				+ "'" // From time stamp
				+ " and time lt datetime"
				+ "'"
				+ toTimestamp
				+ "'" //To time stamp
				+ " "
				+ "and"
				+ " "
				+ property
				+ " le "
				+ rangeTo
				+ "&$select=id, time&$format=json";
		baseurl = baseurl.replaceAll(" ", "%20");
		iotAEUrls.add(baseurl);
		logger.debug("formNoLowerLimitThrehsoldMeasurementUrl: URL is " + baseurl);

		// }
		return iotAEUrls;
	}

	private List<String> formSimpleAggregationUrl(String url, String pst, String fromtimestamp, String toTimestamp, String property, String operator, String value, List<String> equipmentFilters,String dataType) {
		List<String> iotAEUrls = new ArrayList<>();
		if(dataType.equals(Constant.NUMERICFLEXIBLE)){
			value +="f";
		}
		
		if(dataType.equals(Constant.BOOLEAN)){
			value = value.toLowerCase();
		}
		
		// for (String equipmentFilter : equipmentFilters) {
		String baseurl = url
				+ "/"
				+ pst
				+ "/aggregates?$filter=";

		baseurl = baseurl + "time ge datetime"
				+ "'"
				+ fromtimestamp
				+ "'" // From time stamp
				+ " and time lt datetime"
				+ "'"
				+ toTimestamp
				+ "'" //To time stamp
				+ " and  "
				+ property
				+ " "
				+ operator
				+ " "
				+ value
				+ "&$select=id, "
				+ property
				+ "&$format=json";
		baseurl = baseurl.replaceAll(" ", "%20");
		iotAEUrls.add(baseurl);

		logger.debug("formSimpleAggregationUrl: URL is " + baseurl);

		//   }
		return iotAEUrls;
	}

	private List<String> formThresholdAggregationUrl(String url, String pst, String fromtimestamp, String toTimestamp, String property, String rangeFrom, String rangeTo, List<String> equipmentFilters, String dataType) {
		List<String> iotAEUrls = new ArrayList<>();
		if(dataType.equals(Constant.NUMERICFLEXIBLE)){
			rangeFrom +="f";
			rangeTo +="f";
		}
		
		
		//   for (String equipmentFilter : equipmentFilters) {
		String baseurl = url
				+ "/"
				+ pst
				+ "/aggregates?$filter=";

		baseurl = baseurl + "time ge datetime"
				+ "'"
				+ fromtimestamp
				+ "'" // From time stamp
				+ " and time lt datetime"
				+ "'"
				+ toTimestamp
				+ "'" //To time stamp
				+ " and  "
				+ property
				+ " gt "
				+ rangeFrom
				+ " and "
				+ property
				+ " le "
				+ rangeTo
				+ "&$select=id&$format=json";
		baseurl = baseurl.replaceAll(" ", "%20");
		iotAEUrls.add(baseurl);
		logger.debug("formThresholdAggregationUrl: URL is " + baseurl);

		// }
		return iotAEUrls;

	}

	private List<String> formNoUpperLimitThresholdAggregationUrl(String url, String pst, String fromtimestamp, String toTimestamp, String property, String rangeFrom, List<String> equipmentFilters,String dataType) {
		List<String> iotAEUrls = new ArrayList<>();
		if(dataType.equals(Constant.NUMERICFLEXIBLE)){
			rangeFrom +="f";
		}
		//  for (String equipmentFilter : equipmentFilters) {
		String baseurl = url
				+ "/"
				+ pst
				+ "/aggregates?$filter=";

		baseurl = baseurl + "time ge datetime"
				+ "'"
				+ fromtimestamp
				+ "'" // From time stamp
				+ " and time lt datetime"
				+ "'"
				+ toTimestamp
				+ "'" //To time stamp
				+ " and  "
				+ property
				+ " gt "
				+ rangeFrom
				+ "&$select=id&$format=json";
		baseurl = baseurl.replaceAll(" ", "%20");
		iotAEUrls.add(baseurl);
		logger.debug("formNoUpperLimitThresholdAggregationUrl: URL is " + baseurl);

		//  }
		return iotAEUrls;

	}

	private List<String> formNoLowerLimitThresholdAggregationUrl(String url, String pst, String fromtimestamp, String toTimestamp, String property, String rangeTo, List<String> equipmentFilters,String dataType) {
		List<String> iotAEUrls = new ArrayList<>();
		if(dataType.equals(Constant.NUMERICFLEXIBLE)){
			rangeTo +="f";
		}
		// for (String equipmentFilter : equipmentFilters) {
		String baseurl = url
				+ "/"
				+ pst
				+ "/aggregates?$filter=";

		baseurl = baseurl + "time ge datetime"
				+ "'"
				+ fromtimestamp
				+ "'" // From time stamp
				+ " and time lt datetime"
				+ "'"
				+ toTimestamp
				+ "'" //To time stamp
				+ " and  "
				+ property
				+ " le "
				+ rangeTo
				+ "&$select=id&$format=json";
		baseurl = baseurl.replaceAll(" ", "%20");
		iotAEUrls.add(baseurl);
		logger.debug("formNoLowerLimitThresholdAggregationUrl: URL is " + baseurl);

		//  }
		return iotAEUrls;

	}

	private Map<String, Map<Integer, List<String>>> callIoTAPIs(ExecutableRuleJson ruleJson, Map<Integer, List<String>> iotAeUrls, Instant fireTime) throws NamingException {
		logger.debug("Came inside Call to IoT APIs method");
		Map<String, Map<Integer, List<String>>> iotAEResponse = new HashMap<>();
		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		LinkedList<Integer> orderedSequenceNumbers = new LinkedList<Integer>(); 
		List<String> value = new ArrayList<>();
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		String toTimeStamp = sdf.format(Date.from(fireTime));
		int count = 0;

		String authorization = getJwtTokenForIOTAE(ruleJson.getTenantId());
		
		
		
		
		for(RuleStep ruleStep : ruleJson.getRuleSteps()) {
			if (ruleStep.getField1Type() != null && ruleStep.getField1Type().equalsIgnoreCase("AGG")) {
				orderedSequenceNumbers.add(ruleStep.getSequenceNo());
				
			}
				
			}
		for(RuleStep ruleStep : ruleJson.getRuleSteps()) {
			if (ruleStep.getField1Type() != null && ruleStep.getField1Type().equalsIgnoreCase("NORMAL")) {
				logger.debug("evaluateRuleJson: came inside normal conditions");
				if (ruleStep.getField1IsIndicator() != null && ruleStep.getField1IsIndicator().equalsIgnoreCase("X")) { 
					orderedSequenceNumbers.add(ruleStep.getSequenceNo());
				}
			} 
			}
		
		
		
		for(Integer key : orderedSequenceNumbers) {

			value = iotAeUrls.get(key);

			logger.debug("callIoTAPIs:Iterating over iot ae urls for key" + key);
			//if(ruleStepTypeMap.get(1))
			for (String url : value) {
				logger.debug("callIoTAPIs: url got is " + url);
				Response response = clientSetup.get(url, authorization);
				if (response.getStatus() == 200) {
					logger.debug("callIoTAPIs:Call to IoTAE api, status is successful");
					String responseEntity = response.readEntity(String.class);
					JsonElement jelement = new JsonParser().parse(responseEntity);
					JsonObject jobject = jelement.getAsJsonObject();
					jobject = jobject.getAsJsonObject("d");
					JsonArray jarray = jobject.getAsJsonArray("results");
					if (jarray == null || jarray.size() == 0) {
						//Log message. It is possible that output may not have any value.
					} else {
						for (JsonElement jsonElement : jarray) {
							JsonObject properties = jsonElement.getAsJsonObject();
							String timestamp = toTimeStamp;

							if (properties.get("time") != null) {
								String oDataDate = properties.get("time").getAsString();
								String dateInMillis = oDataDate.substring(6, oDataDate.length() - 2);
								timestamp = sdf.format(new Date(Long.parseLong(dateInMillis)));
							}

							if (!iotAEResponse.containsKey(timestamp)) {
								List<String> things = new ArrayList<>();
								Map<Integer, List<String>> ruleStepThingMapping = new HashMap<>();
								things.add(properties.get("id").getAsString());

								ruleStepThingMapping.put(key, things);
								iotAEResponse.put(timestamp, ruleStepThingMapping);


								if(iotAEResponse.containsKey(toTimeStamp) && properties.get("time") != null && !timestamp.equals(toTimeStamp)) {
									count ++;
									iotAEResponse.get(timestamp).putAll(iotAEResponse.get(toTimeStamp));
									
								}
							} else {
								if (!iotAEResponse.get(timestamp).containsKey(key)) {
									List<String> things = new ArrayList<>();
									things.add(properties.get("id").getAsString());
									iotAEResponse.get(timestamp).put(key, things);


								} else {
									iotAEResponse.get(timestamp).get(key).add(properties.get("id").getAsString());
								}
							}


						}
					}
				} else {
					String output = response.readEntity(String.class);
					logger.debug("Cannot fetch indicator value. Reason " + output);
				}
			}

		}
		if(count > 0) {
			iotAEResponse.remove(toTimeStamp);
		}
		return iotAEResponse;
	}

	/* private String getJwtToken(String tenant) throws NamingException {
        String VCAP_SERVICES = System.getenv("VCAP_SERVICES");
        JsonElement jelement = new JsonParser().parse(VCAP_SERVICES);
        JsonObject jobject = new JsonObject();
        JsonObject vcap = jelement.getAsJsonObject();
        JsonArray jarray = vcap.getAsJsonArray("xsuaa");
        int i = 0;
        for(i = 0; i< jarray.size() ; i++) {
            if (jarray.get(i).getAsJsonObject().get("plan").getAsString().equals("broker")) {
                jobject = jarray.get(i).getAsJsonObject();
                logger.debug("Entered the check for application" + jarray.get(i).getAsJsonObject().get("broker").getAsString());
            }
        }
        //jobject = jarray.get(0).getAsJsonObject();

            jobject = jobject.getAsJsonObject("credentials");

        //jobject = jarray.get(0).getAsJsonObject();
        //jobject = jobject.getAsJsonObject("credentials");
        String clientID = jobject.get("clientid").getAsString();
        String httpsProtocol = "https://";
        String stringUrl = httpsProtocol + tenant + "." + jobject.get("uaadomain").getAsString();
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException mfue) {
            logger.error("Malformed Url " + mfue.getMessage());
        }
        UAAServiceInfo uaaServiceInfo = new UAAServiceInfo(tenant,
                jobject.get("clientid").getAsString(), jobject.get("clientsecret").getAsString(),
                jobject.get("identityzone").getAsString(), jobject.get("tenantid").getAsString(),
                jobject.get("tenantmode").getAsString(), jobject.get("uaadomain").getAsString(),
                url, jobject.get("verificationkey").getAsString(),
                jobject.get("xsappname").getAsString());
        UAAClientCredentialsTokenProvider tokenProvider
                = new UAAClientCredentialsTokenProvider(uaaServiceInfo);
        return tokenProvider.getBearerToken(tenant).getAccessToken();
    }*/

	private String getJwtToken(String tenant) throws NamingException {
		JSONObject jobject;
		try {
			jobject = CFEnv.getXSUAACredentials();


			String clientID = jobject.getString("clientid");
			String httpsProtocol = "https://";
			String stringUrl = httpsProtocol + tenant + "." + jobject.getString("uaadomain");
			URL url = null;
			try {
				url = new URL(stringUrl);
			} catch (MalformedURLException mfue) {
				logger.error("Malformed Url " + mfue.getMessage());
			}
			UAAServiceInfo uaaServiceInfo = new UAAServiceInfo(tenant,
					jobject.getString("clientid"), jobject.getString("clientsecret"),
					jobject.getString("identityzone"), jobject.getString("tenantid"),
					jobject.getString("tenantmode"), jobject.getString("uaadomain"),
					url, jobject.getString("verificationkey"),
					jobject.getString("xsappname"));
			UAAClientCredentialsTokenProvider tokenProvider
			= new UAAClientCredentialsTokenProvider(uaaServiceInfo);
			return tokenProvider.getBearerToken(tenant).getAccessToken();
		}catch (JSONException e) {
			logger.error("RuleExecuter :IoT AE Binding expected but couldn't get credentials");
		}
		return null;
	}

	public String getJwtTokenForIOTAE(String tenant) throws NamingException {
		JSONObject credentials;

		try {
			credentials = CFEnv.getIoTAECredentials();
			String httpsProtocol = "https://";
			String stringUrl = httpsProtocol + tenant + "." + credentials.getString("uaadomain");
			URL url = null;
			try {
				url = new URL(stringUrl);
			} catch (MalformedURLException mfue) {
				logger.error("Malformed Url " + mfue.getMessage());
			}

			UAAServiceInfo uaaServiceInfo = new UAAServiceInfo(tenant,
					CFEnv.getIoTAEClientID(), CFEnv.getIoTAEClientSecret(),
					credentials.getString("identityzone"), credentials.getString("tenantid"),
					credentials.getString("tenantmode"), credentials.getString("uaadomain"),
					url, credentials.getString("verificationkey"),
					credentials.getString("xsappname"));
			UAAClientCredentialsTokenProvider tokenProvider =
					new UAAClientCredentialsTokenProvider(uaaServiceInfo);
			return "Bearer " + tokenProvider.getBearerToken(tenant).getAccessToken();
		} catch (JSONException e) {
			logger.error("RuleExecuter :IoT AE Binding expected but couldn't get credentials");
		}
		return null;
	}


	private String executeRuleJson(ExecutableRuleJson ruleJson, Map<String, Map<Integer, List<String>>> iotAEResponse, Instant fireTime, Instant previousFireTime, JSONObject ruleExecutionDetails, String correlationId) {
		logger.debug("executeRuleJson: Enter");
		List<Equipment> equipments = ruleJson.getRuleSubject().getEquipments();
		int noOfEquipmentsExecuted = equipments.size();//Default it to number of equipments - reduce the size when equipment execution fails
		String ruleExecutionStatus = null;
		
		
//		Map<Integer, List<String>> aggregateData = new HashMap<>();
//		Map<Integer, List<String>> indicatorData = new HashMap<>();
//		 Map<String, Map<Integer, List<String>>> iotAEResponseForIndicator = new HashMap<>();
		JSONArray executionDetails = new JSONArray();
		for (Equipment equipment : equipments) {
			logger.debug("executeRuleJson: Iterating for equipment");
			try {
				if (iotAEResponse == null || iotAEResponse.isEmpty()) {
					logger.debug("executeRuleJson: IoTAE urls are empty");
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
					logger.error("executeRuleJson: Timestamp before conversion to String" + fireTime);
					sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
					String timestamp = sdf.format(Date.from(fireTime));
					logger.error("executeRuleJson: Timestamp after conversion to String for alert" + timestamp);
					evaluateRuleJson(ruleJson, timestamp, null, equipment);

				} else {
					logger.debug("executeRuleJson: Executing for only attribute scenarios");

					for (Map.Entry<String, Map<Integer, List<String>>> ruleStepEquipmentMapping : iotAEResponse.entrySet()) {

						logger.debug("executeRuleJson : mapping key final is " + ruleStepEquipmentMapping.getKey());
						logger.debug("executeRuleJson : mapping value final is " + ruleStepEquipmentMapping.getValue());
						evaluateRuleJson(ruleJson, ruleStepEquipmentMapping.getKey(), ruleStepEquipmentMapping.getValue(), equipment);
					}
				}
			} catch (Exception e) {
				logger.error("executeRuleJson: Exception while executing rule json.");
				try {
					JSONObject executionMessage = new JSONObject();
					executionMessage.put("correlationId", correlationId);
					executionMessage.put("equipmentId", equipment.getEquipmentId());
					executionMessage.put("executionStatus", Constant.EXECUTIONSTATUS.FAILURE.getValue());
					executionMessage.put("reason", e.getMessage());
					executionDetails.put(executionMessage);
				} catch (JSONException jsonException) {
					logger.error("JSON parse exception: {} " + jsonException.getMessage());
				}
				noOfEquipmentsExecuted--;
			}
		}
		if (noOfEquipmentsExecuted == equipments.size()) {
			ruleExecutionStatus = Constant.EXECUTIONSTATUS.SUCCESS.getValue();
		} else if (noOfEquipmentsExecuted == 0) {
			ruleExecutionStatus = Constant.EXECUTIONSTATUS.FAILURE.getValue();
		} else {
			ruleExecutionStatus = Constant.EXECUTIONSTATUS.PARTIALLY_SUCCESS.getValue();
		}

		try {
			JSONObject failureReason = new JSONObject();
			failureReason.put("correlationId", correlationId);
			failureReason.put("executionStatus", ruleExecutionStatus);
			failureReason.put("executionDetails", executionDetails);
			ruleExecutionDetails.put(ruleExecutionStatus, failureReason);
		} catch (JSONException jsonException) {
			logger.error("JSON parse exception: {} " + jsonException.getMessage());
		}

		return ruleExecutionStatus;
	}

	private void evaluateRuleJson(ExecutableRuleJson ruleJson, String timestamp, Map<Integer, List<String>> ruleStepEquipmentMapping, Equipment equipment) throws Exception {
		logger.debug("evaluateRuleJson: Enter");
		String expression = "(";
		int previousLevel = 1;
		for (RuleStep ruleStep : ruleJson.getRuleSteps()) {
			logger.debug("evaluateRuleJson: Iterating over rule steps");
			if (ruleStep.getIsStepOperator() == 1) {
				logger.debug("evaluateRuleJson: Step opertor");
				if (ruleStep.getLevel() > previousLevel) {
					expression = expression + "(";
				}
				if (ruleStep.getLevel() < previousLevel) {
					expression = expression + ")";
				}

				if (ruleStep.getOperator().equalsIgnoreCase(Constant.OR)) {
					expression = expression + "||";
				} else {
					expression = expression + "&&";
				}
			} else {
				logger.debug("evaluateRuleJson: Rule steps");
				if (ruleStep.getLevel() > previousLevel) {
					expression = expression + "(";
				}
				if (ruleStep.getLevel() < previousLevel) {
					expression = expression + ")";
				}
				if (ruleStep.getField1Type() != null && ruleStep.getField1Type().equalsIgnoreCase("NORMAL")) {
					logger.debug("evaluateRuleJson: came inside normal conditions");
					if (ruleStep.getField1IsIndicator() != null && ruleStep.getField1IsIndicator().equalsIgnoreCase("X")) {
						logger.debug("evaluateRuleJson: came inside indicator condition");
						if (ruleStepEquipmentMapping == null) {
							logger.debug("evaluateRuleJson: came inside null equipment mapping");
							expression = expression + false;
						} else {
							logger.debug("evaluateRuleJson: came inside not null equipment mapping");
							if (ruleStepEquipmentMapping.containsKey(ruleStep.getSequenceNo())) {
								if (ruleStepEquipmentMapping.get(ruleStep.getSequenceNo()).contains(equipment.getEquipmentExternalId())) {
									logger.debug("evaluateRuleJson: Came inside getting equipment id mapping");
									expression = expression + true;
								} else {
									logger.debug("evaluateRuleJson: came inside false equipment id mapping");
									expression = expression + false;
								}
							} else {
								expression = expression + false;
							}

							logger.debug("evaluateRuleJson: exeting equipment mapping");
						}
						logger.debug("evaluateRuleJson: exiting indicator logic");

					} else {
						logger.debug("evaluateRuleJson: came inside attribute logic");
						expression = expression + ruleStep.getExecutionStatus();
					}
				} else if (ruleStep.getField1Type() != null && ruleStep.getField1Type().equalsIgnoreCase("AGG")) {
					if (ruleStepEquipmentMapping == null) {
						expression = expression + false;
					} else {
						if (ruleStepEquipmentMapping.containsKey(ruleStep.getSequenceNo())) {
							if (ruleStepEquipmentMapping.get(ruleStep.getSequenceNo()).contains(equipment.getEquipmentExternalId())) {
								logger.debug("evaluateRuleJson: Came inside getting equipment id mapping in aggregation scenario");
								expression = expression + true;
							} else {
								logger.debug("evaluateRuleJson: came inside false equipment id mapping in aggregation scenario");
								expression = expression + false;
							}
						} else {
							expression = expression + false;
						}
					}
				}
				logger.debug("evaluateRuleJson: exiting normal logic");
			}
			previousLevel = ruleStep.getLevel();
			logger.debug("evaluateRuleJson: previous level updated");
		}

		for (int index = previousLevel; index >= 1; index--) {
			expression = expression + ")";
		}
		Boolean expRes;
		try {
			logger.debug("Expression is " + expression);
			if (expression.equals("()")) {
				logger.debug("evaluateRuleJson: Empty expression ");
				if (ruleJson.getRuleEvents().get(0).getEventID().equalsIgnoreCase("trigger")) {
					logger.debug("evaluateRuleJson: Trigger based rule. ");
					expRes = true;
				} else {
					logger.debug("evaluateRuleJson: This should never happen.");
					expRes = false;
				}
			} else {
				ExpressionParser parser = new SpelExpressionParser();
				Expression exp = parser.parseExpression(expression);
				expRes = exp.getValue(Boolean.class);
			}

		} catch (Exception e) {
			logger.error("Exception during executing expression.");
			expRes = false;
		}

		//Based on the expression evaluation take the action
		if (expRes) {
			logger.debug("evaluateRuleJson: Expression is true. ");
			List<RuleAction> ruleActions = ruleJson.getRuleActions();
			if (ruleActions == null || ruleActions.isEmpty()) {
				//Logic for PIN can go here.
				logger.debug("evaluateRuleJson: No rule actions are maintained. ");
			} else {
				logger.debug("evaluateRuleJson: Came inside rule action.s ");
				//For normal use case. Creation of alerts is mandatory. Make sure Alerts is present.
				Optional<RuleAction> optionalAlertTypeAction = ruleActions.stream().filter(alertsAction -> alertsAction.getActionId().equalsIgnoreCase("alerttype")).findFirst();
				if (optionalAlertTypeAction.isPresent()) {
					logger.debug("evaluateRuleJson: Alerts present. ");
					//Logic for alerts will come here.
					EmailData emailData = new EmailData();
					RuleAction alertsAction = optionalAlertTypeAction.get();
					List<String> eventIds = new ArrayList<>();
					List<String> correlationIds = new ArrayList<>();
					Boolean alertsCreated = createAlerts(ruleJson, equipment.getEquipmentExternalId(), alertsAction, timestamp, emailData, eventIds, correlationIds);
					if (alertsCreated) {
						logger.debug("evaluateRuleJson: Alerts got created.");
						//Deduplication logic. New alerts, create Email and Notification.
						Optional<RuleAction> optionalSendEmailAction = ruleActions.stream().filter(emailAction -> emailAction.getActionId().equalsIgnoreCase("sendemail")).findFirst();
						if (optionalSendEmailAction.isPresent()) {
							logger.debug("evaluateRuleJson: Logic for email. ");
							RuleAction emailAction = optionalSendEmailAction.get();

							emailData.setEquipmentId(getEquipmentDescription(equipment.getEquipmentId()));

							
							emailData.setSubDomain(ruleJson.getTenantId());

							sendEmail(emailAction, emailData);
						}

						Optional<RuleAction> optionalSendNotificationAction = ruleActions.stream().filter(notificationAction -> notificationAction.getActionId().equalsIgnoreCase("notification")).findFirst();
						if (optionalSendNotificationAction.isPresent()) {
							RuleAction notificationAction = optionalSendNotificationAction.get();
							createNotification(ruleJson.getTenantId(), notificationAction, emailData, eventIds, ruleJson.getClient(), equipment.getEquipmentId());
						}

						//TODO uncomment for sms integrations 
						//                        Optional<RuleAction> optionalSMSAction = ruleActions.stream().filter(smsAction -> smsAction.getActionId().equalsIgnoreCase("sms")).findFirst();
						//                        logger.debug("SMS action check");
						//                        if (optionalSMSAction.isPresent()) {
						//                        	    logger.debug("calling sms service");
						//                            RuleAction smsAction = optionalSMSAction.get();
						//                            sendSMS(smsAction, emailData);
						//                        }
					} else {
						//Deduplication logic. No emails or notification will be raised.
						logger.debug("evaluateRuleJson: Alert didn't get created ");

					}
				} else {
					//Do nothing. Log saying alerts is mandatory.

				}
			}
		}
	}

	// TODO uncomment for sms integeration
	//    private void sendSMS(RuleAction smsAction, EmailData emailData) throws IOException {
	//        logger.debug("send sms function entered");
	//    	    SMSActionParam smsActionParam = new SMSActionParam();
	//        try {
	//        	smsActionParam = new ObjectMapper().readValue(smsAction.getActionParams(), SMSActionParam.class);
	//        } catch (IOException e) {
	//            logger.error("Error while sending sms. " + e.getMessage());
	//        }
	//        String to = smsActionParam.getTo();
	//        
	//        // This query to be changed based on future requirements
	//        String phoneQuery = "select top 1 \"PhoneNumber\" from \"AIN_DEV\".\"sap.ain.metaData::BusinessPartner.PhoneNumber\" "+ 
	//        		"where \"CommunicationDataID\" in (select \"CommunicationDataID\" from \"AIN_DEV\".\"sap.ain.metaData::BusinessPartner.BPCommunicationData\"" +
	//        		"where \"BusinessPartnerID\" = ?)";
	//        logger.debug("query being executed"+phoneQuery);
	//        String phoneNumber = "";
	//        try {
	//           phoneNumber = jdbcTemplate.queryForObject(phoneQuery, new Object[] {to}, String.class);
	//        logger.debug("Result of phone number query" + phoneNumber);
	//        } catch(DataAccessException ex) { 
	//        	  logger.error("Data access exception while fetching phone number using org id");
	//        }
	//        
	//        CloseableHttpClient client = HttpClients.createDefault();
	//        HttpPost httpPost = new HttpPost(
	//                "http://sms-pp.sapmobileservices.com/cmn/sap_intern03881/sap_intern03881.sms");
	//        httpPost.setHeader("Content-Type", "text/plain");
	//        httpPost.setHeader("Authorization", "Basic c2FwX2ludGVybjAzODgxOmhSYzJ4WUVR");
	//
	//        String input = "Subject=GEA SMS Services for notifying Alerts...\n" + "List="+ phoneNumber + "\n"
	//                + "Text=" + emailData.getAlertType() + "present in" + emailData.getEquipmentId() + "\n MobileNotification=YES";
	//        logger.debug("input for sms service" + input);
	//        
	//        StringEntity strentity = new StringEntity(input, ContentType.TEXT_PLAIN);
	////        Entity<StringEntity> entity = Entity.text(strentity);        
	//        httpPost.setEntity(strentity);
	//        
	//        // UsernamePasswordCredentials creds
	//        // = new UsernamePasswordCredentials("sap_intern03881", "hRc2xYEQ");
	//        // httpPost.addHeader(new BasicScheme().authenticate(creds, httpPost, null));
	//        logger.debug("sms service to be called using http client now");
	//        try {
	//            CloseableHttpResponse response = client.execute(httpPost);
	//            logger.debug("sms should have got sent");
	//        } catch (Exception e) {
	//            logger.debug("error in sending sms" + e.getMessage());
	//        }
	//
	//        client.close();
	//	}

	private Boolean createAlerts(ExecutableRuleJson ruleJson, String thing, RuleAction action, String timestamp, EmailData emailData, List<String> eventIds, List<String> correlationIds) throws Exception {
		List<String> alertIds = new ArrayList<>();
		Event events
		= getEvent(ruleJson, thing, action, timestamp, emailData, alertIds);
		String authorization = getJwtTokenForIOTAE(ruleJson.getTenantId());
		
		if (alertIds.size() == 0) {
			logger.error("Alert ID not found");
			throw new Exception("Alert id not found.");
		}
		logger.debug("Alert IDs populated in RuleExecutor" + alertIds.get(0));

		Response response = 
				alertTypeDao.createAlert(authorization, events, alertIds.get(0), ruleJson.getClient(), 
						eventIds, correlationIds, null);

		if (response != null && response.getStatus() == 201) {
			logger.debug("Alert is set for email data correlation id" + correlationIds.get(0));
			logger.debug("Alert is set for email data and length of correlation id is" + correlationIds.size());
			emailData.setAlertId(correlationIds.get(0));
			
			return true;
		} else {
			logger.error("createAlerts:No alerts created, either de duplication is on or error while creating alerts.");
			return false;
		}

	}

	public Event getEvent(ExecutableRuleJson ruleJson, String thing, RuleAction action, String timestamp, EmailData emailData, List<String> alertIds) throws Exception {
		Event events = new Event();
		logger.debug("getEvent:Inside getEvents function");
		try {
			JSONObject jsonObect = new JSONObject(action.getActionParams());

			String code = jsonObect.getString("errorCode");
			if (code == null || code.isEmpty()) {
				code = "None";
			}
			events.setCode(code);

			int severity = jsonObect.getInt("severity");
			String severityDescription = jsonObect.getString("severityDescription");
			
			logger.debug("getEvent():before get alert ID");
			String alertId = jsonObect.getString("alertID");;
			logger.debug("getEvent():got alert ID" + alertId);
			String eventType = getEventTypeId(alertId,ruleJson.getClient());
			logger.debug("getEvent():got eventType" + eventType);
			String status = jsonObect.getString("status");
			String indicatorId = getIndicatorMappedToAlerts(ruleJson.getClient(), alertId);
			String indicatorGroupId = getIndicatorGroupMappedToAlerts(ruleJson.getClient(), alertId);
			alertIds.add(alertId);

			events.setSeverity(severity);
			events.setEventType(eventType);
			events.setStatus(status);

			String property = "";
			if (!indicatorId.equals("")) {
				property = getPropertyForIndicator(ruleJson, null, indicatorId);
			}

			String namedPst = "";
			if (!indicatorGroupId.equals("")) {
				namedPst = getNamedPstForIndicatorGroup(ruleJson, null, indicatorGroupId);
			}

			emailData.setAlertSeverity(severityDescription);
			emailData.setAlertType(getAlertTypeDescription(alertId));
			if (!indicatorId.equals("")) {
			emailData.setAlertProperty(getIndicatorDescription(indicatorId));
			}
			String thingType = ruleJson.getRuleSubject().getModel().getModelExternalId();
			emailData.setModelId(getModelIdDescription(ruleJson.getRuleSubject().getModel().getModelId()));

			if (!Strings.isNullOrEmpty(thingType) && !Strings.isNullOrEmpty(namedPst) && !Strings.isNullOrEmpty(property)) {
				String thingProperty = thingType + "/" + namedPst + "/" + property;
				events.setThingProperty(thingProperty);
			}else{
				events.setThingProperty("");
			}
			
			logger.debug("getEvent Timestamp is new" + timestamp);
			//logger.debug("getEvent Timestamp is" + timestamp);
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			//Date formatedDate = dateFormat.parse(timestamp);
			Date formatedDate = dateFormat.parse(timestamp);
			logger.debug("getEvent:formatedDate post utc timezone" + formatedDate);
			SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d yyyy HH:mm:ss 'GMT'Z (z)");
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			
			String alertCreatedOn = sdf.format(formatedDate);
			logger.debug("getEvent:alertCreatedOn" + alertCreatedOn);
			emailData.setAlertCreatedOn(alertCreatedOn);
			//String businessTimestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(formatedDate);
			//String businessTimestamp = timestamp + "Z" ;
			String businessTimestamp = timestamp + ".000" + "Z" ;
			logger.debug("getEvent:businessTimestamp" + businessTimestamp);
			events.setBusinessTimeStamp(businessTimestamp);
			events.setType(ruleJson.getTenantId().replaceAll("-", ".") + ".pdms.events:AlertsPST");
			events.setDescription(action.getDescription());

			events.setThingId(thing);
			events.setSource("Rule");

			Map<String, Object> alert_details = new HashMap<>();
			alert_details.put("AlertSource", "Rule");
			alert_details.put("AlertDescription", action.getDescription());
			alert_details.put("CreatedTimeStamp",businessTimestamp);
			events.setAlert_details(alert_details);
		} catch (JSONException e) {
			logger.error("Failed while getting event " + e.getMessage());
			throw new Exception("Error while constructing events body. Contact technical support");
		}
		return events;
	}
	
	private String getAlertTypeDescription(String alertType){
		String alertTypeDescription = null;
		String alertTypeDescriptionQuery
		= "select * from \"_SYS_BIC\".\"sap.ain.views.rule/AlertTypeDescriptionForEmail\" \r\n"
				+ "(PLACEHOLDER.\"$$iv_alertTypeId$$\" => ?)";


		try {
			alertTypeDescription = jdbcTemplate.queryForObject(alertTypeDescriptionQuery,
					new Object[]{alertType}, String.class);
			logger.debug("alertTypeDescription.is " + alertTypeDescription);
		} catch (DataAccessException exception) {
			logger.error("Error while getting alertTypeDescription. " + exception.getMessage());
		}

		return alertTypeDescription;
	}
	private String getModelIdDescription(String modelId) {

		String modelDescription = null;
		String alertTypeDescriptionQuery
		= "select * from \"_SYS_BIC\".\"sap.ain.views.rule/ModelDescriptionForEmail\" \r\n"
				+ "(PLACEHOLDER.\"$$modelId$$\" => ?)";


		try {
			modelDescription = jdbcTemplate.queryForObject(alertTypeDescriptionQuery,
					new Object[]{modelId}, String.class);
			logger.debug("modelDescription is " + modelDescription);
		} catch (DataAccessException exception) {
			logger.error("Error while getting modelDescription. " + exception.getMessage());
		}

		return modelDescription;}
	
	
	private String getEquipmentDescription(String equipmentId){
		String equipmentDescription = null;
		String equipmentDescriptionQuery
		= "select * from \"_SYS_BIC\".\"sap.ain.views.rule/EquipmentDescriptionForEmail\" \r\n"
				+ "(PLACEHOLDER.\"$$iv_equipmentId$$\" => ?)";


		try {
			equipmentDescription = jdbcTemplate.queryForObject(equipmentDescriptionQuery,
					new Object[]{equipmentId}, String.class);
			logger.debug("equipmentDescription is " + equipmentDescription);
		} catch (DataAccessException exception) {
			logger.error("Error while getting equipmentDescription. " + exception.getMessage());
		}

		return equipmentDescription;
	}
		
	
	private String getIndicatorDescription(String indicatorId){
		
		String indicatorDescription = null;
		String indicatorDescriptionQuery
		= "select * from \"_SYS_BIC\".\"sap.ain.views.rule/IndicatorDescriptionForEmail\" \r\n"
				+ "(PLACEHOLDER.\"$$iv_indicatorId$$\" => ?)";


		try {
			indicatorDescription = jdbcTemplate.queryForObject(indicatorDescriptionQuery,
					new Object[]{indicatorId}, String.class);
			logger.debug("indicatorDescription is " + indicatorDescription);
		} catch (DataAccessException exception) {
			logger.error("Error while getting indicatorDescription. " + exception.getMessage());
		}

		return indicatorDescription;
	}

	private void sendEmail(RuleAction emailAction, EmailData emailData) {
		String emailSubject = EmailUtils.getEmailSubject(emailData);
		String emailBody = EmailUtils.getEmailBody(emailData);
		logger.error("Email Body Formed " + emailBody);
		EmailActionParam emailActionParam = new EmailActionParam();
		try {
			emailActionParam = new ObjectMapper().readValue(emailAction.getActionParams(), EmailActionParam.class);
		} catch (IOException e) {
			logger.error("Error while sending email. " + e.getMessage());
		}

		List<EmailRecipient> emailRecipients = emailActionParam.getTo();

		List<String> recipients = new ArrayList<>();
		List<GetUsersForAnOrganization> allUsersForAnOrganization
		= getUsersForAnOrganization(emailAction.getClient());
		for (EmailRecipient emailRecipient : emailRecipients) {
			Optional<GetUsersForAnOrganization> optional = allUsersForAnOrganization.stream()
					.filter(user -> user.getPersonId().equals(emailRecipient.getPersonID()))
					.findFirst();
			if (optional.isPresent()) {
				recipients.add("email:" + optional.get().getEmailAddress());
			}
		}

		EmailUtils emailUtils = new EmailUtils();
		EmailAuthenticationInformation emailAuthenticationInformation = emailUtils.getEmailAuthDetailsFromSystemEnv();
		if (emailAuthenticationInformation == null) {
			return;
		}

		Email email = new Email();
		email.setReplyTo(emailAuthenticationInformation.getReplyTo());
		email.setSubject(emailSubject);

		Configuration configuration = new Configuration();
		configuration.setEmail(email);

		EmailNotification emailNotification = new EmailNotification();

		emailNotification.setRecipients(recipients);
		emailNotification.setContentTextType("text/html");
		emailNotification.setContentText(emailBody);
		emailNotification.setContentTextEncoding("NONE");
		emailNotification.setSender(emailAuthenticationInformation.getSender());
		emailNotification.setConfiguration(configuration);

		EmailRequestPayload emailRequestPayload = new EmailRequestPayload();
		emailRequestPayload.SAPnotification = emailNotification;

		try {
			String value = emailAuthenticationInformation.getEmailUrl();

			Entity<EmailRequestPayload> requestPaylod
			= Entity.entity(emailRequestPayload, MediaType.APPLICATION_JSON);

			Response response = clientSetup.post(value, "basic " + Base64.encodeBase64String((emailAuthenticationInformation.getUsername() + ":" + new String(emailAuthenticationInformation.getPassword())).getBytes()),
					requestPaylod);
			logger.error("Response from mail " + response.getStatus());
			
		} catch (Exception e) {
			logger.error("Error while generting emails. " + e.getMessage());
		}

	}

	private List<GetUsersForAnOrganization> getUsersForAnOrganization(String clientId) {

		String getUsersForAnOrganizationQuery
		= "select * from \"_SYS_BIC\".\"sap.ain.views/GetUsersForAnOrganization\" \r\n"
				+ "(PLACEHOLDER.\"$$iv_client$$\" => ?,PLACEHOLDER.\"$$iv_lang$$\" => ?)";

		List<GetUsersForAnOrganization> getUsersForAnOrganization = new ArrayList<>();
		try {
			getUsersForAnOrganization = (List<GetUsersForAnOrganization>) jdbcTemplate.query(
					getUsersForAnOrganizationQuery,
					new Object[]{clientId,
							"en"},
					new GenericRowMapper<GetUsersForAnOrganization>(
							GetUsersForAnOrganization.class));
		} catch (DataAccessException exception) {
			logger.error("Error while getting users. " + exception.getMessage());
		}

		return getUsersForAnOrganization;
	}

	/**
	 * This returns the current timestamp
	 * 
	 * @return
	 */
	public static Timestamp getCurrentTimestamp() {
		DateTime dateTime = new DateTime(DateTimeZone.UTC);
		return new Timestamp(dateTime.getMillis());
	}

	private void createNotification(String tenantId, RuleAction notificationAction, EmailData emailData, List<String> eventIds,
			String client, String equipmentId) throws NamingException {
		logger.debug("Came inside create notification");
		String authorization = getJwtTokenForIOTAE(tenantId);
		
		String ruleId;
		if (eventIds.size() == 1) {
			logger.debug("Event id is " + eventIds.get(0));
		} else {
			logger.debug("Size of event id is " + eventIds.size());
		}

		NotificationActionParam notificationActionParam = new NotificationActionParam();
		try {
			JsonFactory factory = new JsonFactory();
			ObjectMapper mapper = new ObjectMapper(factory);
			String json = new String(notificationAction.getActionParams());
			json = json.replaceAll("\\[|\\]", "");
			Map<String, Object> eventAttributes = mapper.readValue(json, HashMap.class);
			eventAttributes = (Map<String, Object>) eventAttributes.get("to");
			notificationActionParam.setType((String)eventAttributes.get("type"));
			notificationActionParam.setPriority((String)eventAttributes.get("priority"));

		} catch (IOException e) {
			logger.error("Exception while creating notification. " + e.getMessage());
		}
		Rule rule = service.getRule(notificationAction.getRuleID(), em);
		rule.getSystemAdministrativeData().getSystemAdministrativeDataLastChangedByUserID();
		AdminData adminData = new AdminData();
		adminData.setChangedBy(rule.getSystemAdministrativeData().getSystemAdministrativeDataLastChangedByUserID());
		adminData.setCreatedBy(rule.getSystemAdministrativeData().getSystemAdministrativeDataLastChangedByUserID());

		List<String> notificationType = new ArrayList<>();
		notificationType.add("NEW");
		NotificationDescription description = new NotificationDescription();
		getNotificationDescription(description, emailData);
		NotificationPOST post = new NotificationPOST();
		post.setStartDate(new DateTime(getCurrentTimestamp()));
		post.setType(notificationActionParam.getType());
		post.setPriority(notificationActionParam.getPriority());
		post.setStatus(notificationType);
		post.setDescription(description);
		post.setEquipmentID(equipmentId);
		post.setAdminData(adminData);

		if (eventIds.size() == 0) {
			logger.debug("createNotification: No event id found. Notification will be created without linking");
		} else {
			logger.debug("Create notification came inside alert set method");
			AlertPayload alertId = new AlertPayload();
			alertId.setId(eventIds.get(0));
			alertId.setAlertType("");
			post.setAlertIDs(Arrays.asList(alertId));
		}

		NotificationGET notification = notificationServices.createNewNotification(authorization, post);
		logger.debug("Notification created");
		if (notification != null) {
			logger.debug("Notification created {} :" + notification.getNotificationID());
		}
	}

	private void getNotificationDescription(NotificationDescription description,
			EmailData emailData) {
		String alertType = emailData.getAlertType();
		String modelId = emailData.getModelId();
		StringBuilder sb = new StringBuilder("The machine ");
		sb.append(modelId).append(" ").append(emailData.getEquipmentId()).append(" ")
		.append("reported the following error:")
		.append(System.getProperty("line.separator"));
		sb.append(alertType).append(" (")
		.append(emailData.getAlertSeverity()).append(" ")
		.append(emailData.getAlertCreatedOn()).append(")")
		.append(System.getProperty("line.separator"));
		sb.append("Measuring Point: ").append(emailData.getAlertProperty())
		.append(System.getProperty("line.separator"));
		description.setShortDescription(alertType);
		description.setLongDescription(sb.toString());
		description.setLanguageISoCode("en");
	}

	private String getIndicatorMappedToAlerts(String client, String alertId) {
		String query
		= "select \"IndicatorId\" from \"AIN_DEV\".\"sap.ain.metaData::AlertType.AlertTypeIndicatorMapping\" where \"Client\"=? and \"AlertTypeId\" = ?";

		String indicatorId = "";
		try {
			indicatorId = jdbcTemplate.queryForObject(query, new Object[]{client, alertId},
					String.class);
		} catch (DataAccessException exception) {
			logger.error("getIndicatorMappedToAlerts: Data access exception " + exception.getMessage());
			// throw exception;
		}

		return indicatorId;
	}

	private String getIndicatorGroupMappedToAlerts(String client, String alertId) {
		String query
		= "select \"IndicatorGroupId\" from \"AIN_DEV\".\"sap.ain.metaData::AlertType.AlertTypeIndicatorMapping\" where \"Client\"=? and \"AlertTypeId\" = ?";

		String indicatorGroupId = "";
		try {
			indicatorGroupId = jdbcTemplate.queryForObject(query, new Object[]{client, alertId},
					String.class);
		} catch (DataAccessException exception) {
			logger.error("getIndicatorGroupMappedToAlerts: Data access exception " + exception.getMessage());
			// throw exception;
		}

		return indicatorGroupId;
	}

	private String getAlertId(String eventType, String client) throws Exception {
		logger.debug("getAlertId: Enter");
		String alertId = null;
		String alertIdQuery
		= "select \"ID\" from \"AIN_DEV\".\"sap.ain.metaData::AlertType.AlertTypeExternalIdTable\" where \"ExternalID\" = ? and \"Client\" = ?";

		try {
			alertId = jdbcTemplate.queryForObject(alertIdQuery,
					new Object[]{eventType,client}, String.class);

		} catch (Exception e) {
			logger.error("getAlertId: Error while fetching alert id " + e.getMessage());
			throw new Exception("getAlertId:Error while fetching alert id");
		}

		if (alertId == null) {
			logger.error("getAlertId: Cannot fetch alert id");
			throw new Exception("getAlertId: Cannot fetch alert id");
		}

		return alertId;
	}
	
	private String getEventTypeId(String alertId, String client) throws Exception {
		logger.debug("getAlertId: Enter");
		String eventType = null;
		String alertIdQuery
		= "select \"ExternalID\" from \"AIN_DEV\".\"sap.ain.metaData::AlertType.AlertTypeExternalIdTable\" where \"ID\" = ? and \"Client\" = ?";

		try {
			eventType = jdbcTemplate.queryForObject(alertIdQuery,
					new Object[]{alertId,client}, String.class);

		} catch (Exception e) {
			logger.error("getEventTypeId: Error while fetching event type id " + e.getMessage());
			throw new Exception("Replication of one or more alert type to IoTae has failed. Please publish the equipment");
		}

		if (eventType == null) {
			logger.error("getEventTypeId: Cannot fetch alert id");
			throw new Exception("Replication of one or more alert type to IoTae has failed. Please publish the equipment");
		}

		return eventType;
	}

	private String getTenantId(String clientId) {
		String tenantId = null;
		String query
		= "Select DISTINCT(\"HCPAccountDetails\") as \"TenantID\" from \"AIN_DEV\".\"sap.ain.metaData::BusinessPartner.OrganizationName\" "
				+ "where \"BusinessPartnerID\"= ?";
		try {
			tenantId = jdbcTemplate.queryForObject(query, new Object[]{clientId}, String.class);
		} catch (DataAccessException exception) {
			logger.error(
					"Data access exception occurred while getting tenant id for a given client id... in RuleExecuter.getTenantId");
		}
		return tenantId;
	}

	private String getUserBpIdOfRule(String ruleId) {
		String userBpId = null;
		String query
		= "Select \"SystemAdministrativeData.CreatedByUser.ID\" from \"AIN_DEV\".\"sap.ain.metaData::Rules.Rule\" "
				+ " where \"ID\" = ?";
		try {
			userBpId = jdbcTemplate.queryForObject(query, new Object[]{ruleId}, String.class);
		} catch (DataAccessException exception) {
			logger.error(
					"Data access exception occurred while getting user bp id for a given rule id... in RuleExecuter.getUserBpIdOfRule");
		}
		return userBpId;
	}

}

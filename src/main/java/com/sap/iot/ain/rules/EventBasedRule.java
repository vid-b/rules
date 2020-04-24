package com.sap.iot.ain.rules;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.naming.NamingException;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sap.iot.ain.template.dao.AlertTypeDao;
import com.sap.iot.ain.template.payload.Event;
import com.sap.iot.ain.core.AINObjectTypes;
import com.sap.dsc.ac.iotae.utils.IOTAeConstants;
import com.sap.iot.ain.reuse.AINConstants;
import com.sap.iot.ain.reuse.Strings;
import com.sap.iot.ain.reuse.utils.ObjectUtils;
import com.sap.iot.ain.rules.implementation.CFEnv;
import com.sap.iot.ain.rules.implementation.RuleExecutor;
import com.sap.iot.ain.rules.models.AlertType;
import com.sap.iot.ain.rules.models.Equipment;
import com.sap.iot.ain.rules.models.GetRulesForAlert;
import com.sap.iot.ain.rules.services.RuleService;
import com.sap.iot.ain.rules.uaa.UAAClientCredentialsTokenProvider;
import com.sap.iot.ain.rules.uaa.UAAServiceInfo;

@Component
public class EventBasedRule {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RuleExecutor rulesExecuter;

    @Autowired
    private AlertTypeDao alertTypeDao;
    
    @Autowired
    RuleService service;

    private static final Logger logger = LoggerFactory.getLogger(EventBasedRule.class);
    public static final int ID_CHUNK_SIZE = 150;

    public void executeEventBasedRule(String dataReceivedFromMQ) {
        String corrleationId = UUID.randomUUID().toString();
        MDC.put(AINConstants.CORRELATION_ID, corrleationId);
        String[] splittedDataReceivedFromMQ = dataReceivedFromMQ.split("/");
        String tenantId = splittedDataReceivedFromMQ[0];
        String clientId = getClientId(tenantId);
        if (!Strings.isNullOrEmpty(clientId)) {
            String alertTypeIdBitStringRecievedFromMQ = splittedDataReceivedFromMQ[6];
            String[] splittedAlertTypeIds = alertTypeIdBitStringRecievedFromMQ.split(";");
            List<AlertType> alertTypes = getAlertTypeDetails(clientId, splittedAlertTypeIds);
            if (!ObjectUtils.isListEmpty(alertTypes)) {
                evaluateAlerts(clientId, alertTypes, splittedDataReceivedFromMQ, corrleationId);
            }
        }
    }
    

    private void evaluateAlerts(String clientId, List<AlertType> alertTypes,
            String[] splittedDataReceivedFromMQ, String correlationId) {
        String equipmentId = getAINObjectIdForExternalId(clientId, splittedDataReceivedFromMQ[2], "pdmsSysThing", AINObjectTypes.EQU.toString());
        if (!Strings.isNullOrEmpty(equipmentId)) {
            Equipment equipment = new Equipment();
            equipment.setEquipmentId(equipmentId);
            equipment.setEquipmentExternalId(splittedDataReceivedFromMQ[2]);

            long milliSeconds = Long.parseLong(splittedDataReceivedFromMQ[5]);
            Date date = new Date(milliSeconds);
            long secondDifference = milliSeconds - (1000);
            Date previousDate = new Date(secondDifference);

            List<String> alertTypeIds = Arrays.asList(splittedDataReceivedFromMQ[6].split(";"));

            List<GetRulesForAlert> getRulesForAlerts = getRuleCreatedForEvent(clientId, equipmentId, alertTypeIds);

            List<String> getSuppressesAlertTypeIds = new ArrayList<>();
            if (!ObjectUtils.isListEmpty(getRulesForAlerts)) {
                getRulesForAlerts.forEach(ruleId -> {
                    logger.error("Rule {} exists for alertType " + ruleId.getRuleId());
                    logger.error("Client {} exists for alertType " + ruleId.getClient());
                    String exeuctionId = date.getTime() / 1000 + ruleId.getRuleId().substring(0, 19);
                    service.persistRuleRun(ruleId.getRuleId(), exeuctionId, ruleId.getClient(), date.toInstant());
                    rulesExecuter.execute(ruleId.getRuleId(), date.toInstant(), previousDate.toInstant(), equipment, exeuctionId, correlationId);
                });
                getSuppressesAlertTypeIds = getRulesForAlerts.stream().map(rule -> rule.getAlertTypeId()).collect(Collectors.toList());
            }

            for (AlertType alertType : alertTypes) {
                if (!getSuppressesAlertTypeIds.contains(alertType.getAlertTypeId())) {
                    logger.error("Directly creating event for alertType");
                    try {
                        createEvent(clientId, alertType, splittedDataReceivedFromMQ);
                    } catch (Exception ex) {
                        logger.error("Exception while creating event " + ex.getMessage());
                    }
                }
            }
        }

    }

    private String getClientId(String tenantId) {
        String clientId = null;
        String query
                = "select distinct(A.\"BusinessPartnerID\") as \"ClientID\"\n"
                + "from  \"AIN_DEV\".\"sap.ain.metaData::BusinessPartner.OrganizationName\" as A\n"
                + "left join \"AIN_DEV\".\"sap.ain.metaData::BusinessPartner.OrgAdditionalDetails\" as B\n"
                + "on A.\"BusinessPartnerID\" = B.\"Client\"\n"
                + "where A.\"HCPAccountDetails\" = ? and B.\"AccountType\" = '1' and B.\"isActive\" = 1";
        try {
            clientId = jdbcTemplate.queryForObject(query, new Object[]{tenantId}, String.class);
        } catch (DataAccessException exception) {
            logger.error(
                    "Data access exception occurred while getting client id for a given tenant id... in EventBasedRule.getClientId");
        }
        return clientId;
    }

//    private List<AlertType> getAlertTypeDetails(String clientId, String[] alertTypeIds) {
//        if (alertTypeIds == null || alertTypeIds.length == 0) {
//            return null;
//        }
//        
//       /*String getAlertTypeDetailsQuery = 
//        		"	SELECT \"Hdr\".\"ID\" AS \"ID\", \"Hdr\".\"AlertTypeID\" AS \"AlertTypeID\", \"Hdr\".\"ExternalID\" AS \"ExternalID\", " 
//        		+"	\"Hdr\".\"Severity\" AS \"Severity\", \"Hdr\".\"ErrorCode\" AS \"ErrorCode\", "
//        		+"	SUBSTR_BEFORE (\"Hdr\".\"ExternalID\", '.pdms.events') AS \"TenantSubDomain\" , \"Desc\".\"Description\" AS \"Description\" "
//        		+"	FROM \"AIN_DEV\".\"sap.ain.metaData::AlertType.Header\" AS \"Hdr\" "
//        		+"	INNER JOIN \"sap.ain.metaData::AlertType.Description\" AS \"Desc\" "
//        		+"	ON \"Hdr\".\"ID\" = \"Desc\".\"ID\" "
//        		+"	WHERE (\"ExternalID\" IS NOT NULL AND \"ExternalID\" <> '') " 
//        		+"	AND \"Hdr\".\"Client\" = ? AND \"AlertTypeID\" IN ( "
//        		+	ObjectUtils.getQuestionMark(alertTypeIds.length) + ");";*/
//        
//        String getAlertTypeDetailsQuery = 
//        		"SELECT \"Hdr\".\"ID\" AS \"ID\"," + 
//        		" \"Hdr\".\"AlertTypeID\" AS \"AlertTypeID\"," + 
//        		" \"Ext\".\"ExternalID\" AS \"ExternalID\", " + 
//        		" \"Hdr\".\"Severity\" AS \"Severity\"," + 
//        		" \"Hdr\".\"ErrorCode\" AS \"ErrorCode\"," + 
//        		" SUBSTR_BEFORE (\"Ext\".\"ExternalID\", '.pdms.events') AS \"TenantSubDomain\" ," + 
//        		" \"Desc\".\"Description\" as \"Description\"" + 
//        		               
//        		" FROM \"AIN_DEV\".\"sap.ain.metaData::AlertType.Header\" AS \"Hdr\"" + 
//        		" INNER JOIN \"sap.ain.metaData::AlertType.Description\" AS \"Desc\"" + 
//        		" ON \"Hdr\".\"ID\" = \"Desc\".\"ID\" and" + 
//        		" \"Hdr\".\"Client\" = \"Desc\".\"Client\"" + 
//        		" inner join \"AIN_DEV\".\"sap.ain.metaData::AlertType.AlertTypeExternalIdTable\" AS \"Ext\"" + 
//        		" on \"Hdr\".\"ID\" = \"Ext\".\"ID\"\n" + 
//        		" and \"Hdr\".\"Client\" = \"Ext\".\"Client\"\n" + 
//        		" WHERE (\"Ext\".\"ExternalID\" IS NOT NULL AND \"Ext\".\"ExternalID\" <> '')" + 
//        		"AND \"Ext\".\"Client\" = ? AND \"Hdr\".\"AlertTypeID\" IN ( " + 
//        		 ObjectUtils.getQuestionMark(alertTypeIds.length) + ");";
//        
//     
//        		
//        		
//
//        ArrayList<String> idList = new ArrayList<>();
//        idList.addAll(Arrays.asList(alertTypeIds));
//        idList.add(0, clientId);
//
//        List<AlertType> alertTypeDetails
//                = jdbcTemplate.query(getAlertTypeDetailsQuery, idList.toArray(), new AlertType());
//        return alertTypeDetails;
//    } 
    
    private List<AlertType> getAlertTypeDetails(String clientId, String[] alertTypeIds) {
    	List<AlertType> alertTypeDetails = new ArrayList<AlertType>();
    	List<AlertType> alertTypes = new ArrayList<AlertType>();
    	if (alertTypeIds == null || alertTypeIds.length == 0) {
    		return null;
    	}


    	String getAlertTypeDetailsQuery  = "SELECT \"Hdr\".\"ID\",\n" + 
    			"        \"Hdr\".\"AlertTypeID\",\n" + 
    			"        \"Hdr\".\"ExternalID\", \n" + 
    			"        \"Hdr\".\"Severity\" ,\n" + 
    			"         \"Hdr\".\"ErrorCode\" ,\n" + 
    			"         \"Hdr\".\"TenantSubDomain\",\n" + 
    			"         \"Hdr\".\"Description\" from\n" + 
    			"         \"_SYS_BIC\".\"sap.ain.views.rule/AlertTypeWithDescription\"(PLACEHOLDER.\"$$iv_alerttype_list$$\" =>? , PLACEHOLDER.\"$$iv_clientId$$\" => ?) as \"Hdr\"\n" + 
    			"         ";
    	try {

    		// Processing the list in chunks as HANA nvarchar limitation is 5000
    		// (150 * 32 < 5000)
    		for (int i = 0; i < alertTypeIds.length; i += ID_CHUNK_SIZE) {

    			String[] chunk = Arrays.copyOfRange(alertTypeIds,i, Math.min(alertTypeIds.length, i + ID_CHUNK_SIZE));

    			Object[] viewParams = new Object[]{
    					getIDsWithDelimiter(chunk, ","),clientId};

    			alertTypeDetails
    			= jdbcTemplate.query(getAlertTypeDetailsQuery, viewParams, new AlertType());

    			alertTypes.addAll(alertTypeDetails);
    		} 

    	} catch (DataAccessException e) {
    			logger.error("Inside getAlertTypeDetails : Error while fetching alert types");
    	}

    	return alertTypes;	

    }

    private List<GetRulesForAlert> getRuleCreatedForEvent(String clientId, String equipmentId, List<String> alertTypeIds) {
        List<GetRulesForAlert> ruleIds = new ArrayList<>();
        String query = "Select distinct \"RuleID\", \"AlertTypeID\",\"Client\" from \"_SYS_BIC\".\"sap.ain.views.rule/Rules\" "
                + " (PLACEHOLDER.\"$$iv_client$$\" => ?) "
                + "where \"EventID\" = 'trigger' and \"ActivationStatus\" = 'Activated' and \"IsEnabled\"='true' "
                + " and \"EquipmentID\" = ? and \"AlertTypeID\" IN ("
                + ObjectUtils.getQuestionMark(alertTypeIds.size()) + ");";

        ArrayList<String> idList = new ArrayList<>();
        idList.addAll(alertTypeIds);
        idList.add(0, equipmentId);
        idList.add(0, clientId);

        try {
            ruleIds = jdbcTemplate.query(query, idList.toArray(),
                    new GetRulesForAlert());
        } catch (DataAccessException exception) {
            logger.error(
                    "Data access exception occurred while getting rule for an event... in EventBasedRule.getRuleCreatedForEvent");
        }
        return ruleIds;
    }

    private void createEvent(String clientId, AlertType alertType,
            String[] splittedDataReceivedFromMQ) throws Exception {
        logger.debug("Came inside create Event method");

        long milliSeconds = Long.parseLong(splittedDataReceivedFromMQ[5]);
        
        Date date = new Date(milliSeconds);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        String businessTimestamp = sdf.format(Date.from(date.toInstant()));
      

        Event event = new Event();
        event.setCode(alertType.getErrorCode());
        event.setSeverity(alertType.getSeverity());
        event.setEventType(alertType.getExternalId());
        event.setStatus(IOTAeConstants.EVENT_STATUSES.NEW.getStatus());
        event.setType(alertType.getTenantSubDomain() + ".pdms.events:AlertsPST");
        event.setDescription(alertType.getAlertTypeDescription());
        event.setBusinessTimeStamp(businessTimestamp);

        event.setThingId(splittedDataReceivedFromMQ[2]);
        logger.debug("Thing id is  " + splittedDataReceivedFromMQ[2]);
        event.setSource("Machine");
        String alertId = alertType.getId();
        String indicatorId = getIndicatorMappedToAlerts(clientId, alertId);
        logger.debug("Indicator id is " + indicatorId);
        String indicatorGroupId = getIndicatorGroupMappedToAlerts(clientId, alertId);
        logger.debug("indicator group id is " + indicatorGroupId);

        String thingType = splittedDataReceivedFromMQ[1];
        logger.debug("thing type is " + thingType);

        String modelId = getModelIdFromThingType(clientId, splittedDataReceivedFromMQ[1]);
        logger.debug("model id is " + modelId);

        String property = "";
        if (!indicatorId.equals("")) {
            property = getPropertyForIndicator(clientId, modelId, indicatorId);
        }
        logger.debug("Property is " + property);

        String namedPst = "";
        if (!indicatorGroupId.equals("")) {
            namedPst = getNamedPstForIndicatorGroup(clientId, modelId, indicatorGroupId);
        }
        
        logger.debug("named pst is " + namedPst);


        if (!Strings.isNullOrEmpty(thingType) && !Strings.isNullOrEmpty(namedPst) && !Strings.isNullOrEmpty(property)) {
            String thingProperty = thingType + "/" + namedPst + "/" + property;
            event.setThingProperty(thingProperty);
        }else{
        	
			event.setThingProperty("");
		}

        Map<String, Object> alert_details = new HashMap<>();
        alert_details.put("AlertSource", "Machine");
        alert_details.put("AlertDescription", alertType.getAlertTypeDescription());
        alert_details.put("CreatedTimeStamp", businessTimestamp);
        event.setAlert_details(alert_details);

        try {
            String authorization = getJwtTokenForIOTAE(splittedDataReceivedFromMQ[7]);
            
            alertTypeDao.createAlert(authorization, event, alertType.getId(), clientId, null);
        } catch (Exception e) {
            logger.error("Exception occured in EventBasedRule.createEvent" + e.getMessage());
        }

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

    private String getNamedPstForIndicatorGroup(String client, String modelId, String indicatorGroup) throws Exception {
        logger.debug("getNamedPstForIndicatorGroup: Enter");
        String pst = null;
        String pstQuery
                = "Select DISTINCT(\"NAMED_PST_ID\") as \"PST_NAME\" from \"sap.ain.metaData::Configurations.ExternalSystemMapping\" where "
                + " \"GROUP_ID\"= ? and \"SYSTEM_NAME\"= ?  and \"MAPPING_FLAG\"='0' and \"CLIENT\" = ? and \"MODEL_ID\"= ? and \"IS_VALID\" = 1";

        try {
            pst = jdbcTemplate.queryForObject(pstQuery,
                    new Object[]{indicatorGroup, IOTAeConstants.SYSTEM_NAME_PACKAGE, client, modelId}, String.class);

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

    private String getPropertyForIndicator(String client, String modelId, String indicator) throws Exception {
        logger.debug("getPropertyForIndicator: Enter");
        String property = null;
        String propertyQuery
                = "Select DISTINCT(\"PROPERTY_ID\") as \"PROPERTY_ID\" from "
                + " \"sap.ain.metaData::Configurations.ExternalSystemMapping\" where  \"ATTRIBUTE_ID\" = ? and \"SYSTEM_NAME\"= ? and \"MAPPING_FLAG\"='0' "
                + "and \"CLIENT\" =? and \"MODEL_ID\"= ? and \"IS_VALID\" = 1";

        try {
            property = jdbcTemplate.queryForObject(propertyQuery,
                    new Object[]{indicator, IOTAeConstants.SYSTEM_NAME_PACKAGE, client, modelId}, String.class);

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

    private void createEventOld(String clientId, AlertType alertType,
            String[] splittedDataReceivedFromMQ) {
        Event event = new Event();
        event.setCode(alertType.getErrorCode());
        event.setSeverity(alertType.getSeverity());
        event.setEventType(alertType.getExternalId());
        event.setStatus(IOTAeConstants.EVENT_STATUSES.NEW.getStatus());

        long milliSeconds = Long.parseLong(splittedDataReceivedFromMQ[5]);
        Date date = new Date(milliSeconds);

        String businessTimestamp = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'").format(date);

        event.setBusinessTimeStamp(businessTimestamp);
        Map<String, Object> alert_details = new HashMap<>();
        alert_details.put("AlertSource", "Machine");
        alert_details.put("AlertDescription", alertType.getAlertTypeId());
        alert_details.put("CreatedTimeStamp", businessTimestamp);
        event.setAlert_details(alert_details);

        event.setType(alertType.getTenantSubDomain() + ".pdms.events:AlertsPST");
        event.setDescription("Event type description");
        event.setThingId(splittedDataReceivedFromMQ[2]);
        event.setThingProperty(splittedDataReceivedFromMQ[1] + "/" + splittedDataReceivedFromMQ[3] + "/" + splittedDataReceivedFromMQ[4]);
        try {
            String jwt = getJwtToken(alertType.getTenantSubDomain().replaceAll("\\.", "-"));
            String authorization = "Bearer " + jwt;
            alertTypeDao.createAlert(authorization, event, alertType.getId(), clientId, null);
        } catch (Exception e) {
            logger.error("Exception occured in EventBasedRule.createEvent");
        }
    }

     private String getJwtToken(String tenant) throws NamingException {
        String VCAP_SERVICES = System.getenv("VCAP_SERVICES");
        JsonElement jelement = new JsonParser().parse(VCAP_SERVICES);
        JsonObject jobject = new JsonObject();
        JsonObject vcap = jelement.getAsJsonObject();
        JsonArray jarray = vcap.getAsJsonArray("xsuaa");
        int i = 0;
        for(i = 0; i< jarray.size() ; i++) {
            if (jarray.get(i).getAsJsonObject().get("plan").getAsString().equals("application")) {
                jobject = jarray.get(i).getAsJsonObject();
                logger.debug("Entered the check for application" + jarray.get(i).getAsJsonObject().get("plan").getAsString());
            }
        }
        //jobject = jarray.get(0).getAsJsonObject();

            jobject = jobject.getAsJsonObject("credentials");

        //jobject = jarray.get(0).getAsJsonObject();
        //jobject = jobject.getAsJsonObject("credentials");
        String clientID = jobject.get("clientid").getAsString();
        String httpsProtocol = "https://";
        String stringUrl = httpsProtocol + tenant + "." + jobject.get("uaadomain").getAsString();
        logger.info("URL: {}" + stringUrl);
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException mfue) {
            logger.error("Malformed url in EventBasedRule.getJwtToken");
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
    }
     
     private String getJwtTokenForIOTAE(String tenant) throws NamingException {
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


    private String getAINObjectIdForExternalId(String clientId, String externalId,
            String systemName, String objectType) {
        String externalSysId = getExternalSystemId(clientId, systemName);

        String ainObjectIdForExternalIdQuery = " select \"ExternalIDMapping\".\"AINObjectID\"   from "
                + "\"sap.ain.metaData::Configurations.ExternalIDMapping\" as \"ExternalIDMapping\" where \"ExternalIDMapping\".\"Client\" = ?"
                + "and \"ExternalIDMapping\".\"ID\" = ? and \"ExternalIDMapping\".\"ObjectType\" = ? and "
                + " \"ExternalIDMapping\".\"ExternalID\" = ?";

        String ainObjectId = "";
        try {
            ainObjectId = jdbcTemplate.queryForObject(ainObjectIdForExternalIdQuery,
                    new Object[]{clientId, externalSysId, objectType, externalId}, String.class);
        } catch (DataAccessException exception) {
            logger.error("Exception occured at EventBasedRule.getAINObjectIdForExternalId{} ",
                    exception.getMessage());
        }
        return ainObjectId;
    }

    private String getExternalSystemId(String clientId, String systemName) {
        String query
                = "SELECT top 1 \"ID\" FROM \"_SYS_BIC\".\"sap.ain.views/ExternalSystemsList\" (PLACEHOLDER.\"$$iv_client$$\" => ? ,PLACEHOLDER.\"$$iv_lang$$\"=>?)"
                + " where \"SystemStatusDescription\" = 'Active' and \"SystemType\" = 'PdMS' and \"SystemName\" = ?";

        String extSysId = "";
        try {
            extSysId = jdbcTemplate.queryForObject(query, new Object[]{clientId, "en", systemName},
                    String.class);
        } catch (DataAccessException exception) {
            logger.error("Exception occured at EventBasedRule.getExternalSystemId{} ",
                    exception.getMessage());
        }

        return extSysId;
    }

    private String getModelIdFromThingType(String client, String thingTypeName) {
        String modelId = "";

        String query
                = "select top 1 \"Header\".\"ID\" from \"AIN_DEV\".\"sap.ain.metaData::Model.Header\" \"Header\" join  \"AIN_DEV\".\"sap.ain.metaData::Configurations.ExternalSystemMapping\"  \"Ext\"\n" + 
                		"on \"Header\".\"ID\" = \"Ext\".\"MODEL_ID\" where \"Ext\".\"CLIENT\" = ? and \"Ext\".\"THING_TYPE_NAME\" = ? and \"Ext\".\"IS_VALID\" = 1 and \"Header\".\"isMarkedForDeletion\" = 0";

        try {
            modelId = jdbcTemplate.queryForObject(query, new Object[]{client,thingTypeName},
                    String.class);
        } catch (DataAccessException exception) {
            logger.error("Exception occured at EventBasedRule.getExternalSystemId{} ",
                    exception.getMessage());
        }

        return modelId;
    }
    
    public static String getIDsWithDelimiter(String[] ids, String delimiter) {

		String idsWithComma = "";
		if (ids == null) {
			return idsWithComma;
		}

		for (int i = 0; i < ids.length; i++) {
			if (i + 1 == ids.length) {
				idsWithComma = idsWithComma + ids[i];
			} else {
				idsWithComma = idsWithComma + ids[i] + delimiter;
			}
		}

		return idsWithComma;
	}


}

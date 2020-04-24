package com.sap.iot.ain.rules.controller;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.iot.ain.reuse.utils.EnvironmentUtils;
import com.sap.iot.ain.rules.implementation.RuleExecutor;
import com.sap.iot.ain.rules.models.AttributeValue;
import com.sap.iot.ain.rules.models.Equipment;
import com.sap.iot.ain.rules.models.RuleWithSteps;
import com.sap.iot.ain.rules.services.RuleService;
import com.sap.iot.ain.template.dao.AlertTypeDao;
import com.sap.xs2.security.container.SecurityContext;
import com.sap.xs2.security.container.UserInfoException;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.slf4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.naming.NamingException;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;


@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PrepareForTest({ EnvironmentUtils.class , SecurityContext.class , RuleExecutor.class, Logger.class})
public class RuleExecuteEquipmentTest {
    static String  ruleId = "40f814ac6c3f488cadd0235fcc8ccc19";
    Instant fireTime = Instant.now();
    Instant previousFireTime = Instant.now().minusSeconds(60*2);
    Equipment equipment = null;
    String executionId = null;
    String correlationId= null;



    DriverManagerDataSource dataSource = new DriverManagerDataSource();

    @Mock
    RuleService service = new RuleService();

    @Mock
    JdbcTemplate jdbcTemplate;

    @Mock
    AlertTypeDao alertTypeDao = new AlertTypeDao();

    @Mock
    Logger log ;

    @Spy
    @InjectMocks
    RuleExecutor executer =  new RuleExecutor();

    ObjectMapper mapper = new ObjectMapper();

    JSONObject json = null;

    @Before
    public void setup() throws IOException, JSONException, NamingException, UserInfoException, java.text.ParseException, ParseException, JSONException {
        MockitoAnnotations.initMocks(this);
        File rule_details  =  new File("src/test/java/com/sap/iot/ain/rules/cron/Rule_Details_Equipment.json");
        File equip_ment_list  =  new File("src/test/java/com/sap/iot/ain/rules/cron/EquipmentList.json");
        RuleWithSteps steps = mapper.readValue(rule_details, RuleWithSteps.class);
        List<Equipment> eqlist = mapper.readValue(equip_ment_list,new TypeReference<List<Equipment>>(){} );
        Mockito.when(service.getRuleDetails(any(),any())).thenReturn(steps);
        Mockito.when(alertTypeDao.createAlert(any(),any(),any(),any(),any(),any(),any())).thenReturn(Response.status(201).entity("Successfully Created Alert").build());
        try{
            PowerMockito.doReturn("7DD9CD646B1E454C8324D760B346E9B0").when(executer, "getExternalSystemId", anyString(),anyString());
            PowerMockito.doReturn(eqlist).when(executer, "getEquipmentDetails", anyString(),anyString(),anyString());
            PowerMockito.doReturn("dca.test.sap.mcmaug5:M_MCMAug5").when(executer, "getExternalIdForModel", anyString(),anyString(),anyString());
            PowerMockito.doReturn("https://analytics-thing-sap-stakeholder.cfapps.sap.hana.ondemand.com").when(executer, "getExternalSystemURL", anyString(),anyString());
            PowerMockito.doReturn("BPID").when(executer, "getUserBpIdOfRule", anyString());
            PowerMockito.doReturn("TID").when(executer, "getTenantId", anyString());
            PowerMockito.doReturn("ALERT-ID").when(executer, "getEventTypeId", anyString(),anyString());
            PowerMockito.doReturn("INDICATOR-ID").when(executer, "getIndicatorMappedToAlerts", anyString(),anyString());
            PowerMockito.doReturn("INDICATOR-GROUP-ID").when(executer, "getIndicatorGroupMappedToAlerts", anyString(),anyString());
            PowerMockito.doReturn("PROPERTY-FOR-INDICATOR").when(executer, "getPropertyForIndicator",any(), any(),any());
            PowerMockito.doReturn("PROPERTY-FOR-INDICATOR-GROUP").when(executer, "getNamedPstForIndicatorGroup",any(), any(),any());
            PowerMockito.doNothing().when(executer,"sendEmail",any(),any());
            PowerMockito.doNothing().when(executer,"createNotification",any(),any(),any(),any(),any(),any());
            Mockito.doReturn("Bearer Token").when(executer).getJwtTokenForIOTAE(Mockito.anyString());
            Mockito.doNothing().when(service).updateRule(any(),any());
            Mockito.doNothing().when(service).persistRuleExecutionLogs(any(),any(),any(),any());
            Whitebox.setInternalState(RuleExecutor.class, "logger", log);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void execute_rule_equipment() throws JSONException , NamingException, Exception {
        AttributeValue attribute = new AttributeValue();
        attribute.setBooleanValue("1");
        List<AttributeValue> value = new ArrayList<>();
        value.add(attribute);
        Mockito.when(jdbcTemplate.query(anyString(),any(Object[].class),Matchers.<RowMapper<AttributeValue>>any())).thenReturn(value);
        executer.execute(ruleId ,fireTime,previousFireTime ,equipment, executionId , correlationId);
        PowerMockito.verifyPrivate(executer, times(1)).invoke("getEquipmentDetails", anyString(),anyString(),anyString());
        PowerMockito.verifyPrivate(executer, times(1)).invoke("getExternalIdForModel", anyString(),anyString(),anyString());
        PowerMockito.verifyPrivate(executer, times(1)).invoke("getExternalSystemURL", anyString(),anyString());
        PowerMockito.verifyPrivate(executer, times(1)).invoke("getAttributeValueForEquipment", anyString(),anyString(),anyString(),anyString(),anyString(),anyString());
        PowerMockito.verifyPrivate(executer, times(1)).invoke("getUserBpIdOfRule", anyString());
        PowerMockito.verifyPrivate(executer, times(1)).invoke("getTenantId", anyString());
        PowerMockito.verifyPrivate(executer, times(1)).invoke("getEventTypeId", anyString(),anyString());
        PowerMockito.verifyPrivate(executer, times(1)).invoke("getIndicatorMappedToAlerts", anyString(),anyString());
        PowerMockito.verifyPrivate(executer, times(1)).invoke("getIndicatorGroupMappedToAlerts", anyString(),anyString());
        PowerMockito.verifyPrivate(executer, times(1)).invoke("getPropertyForIndicator", any(), any(),any());
        Mockito.verify(alertTypeDao,times(1)).createAlert(any(),any(),any(),any(),any(),any(),any());

    }

    @Test
    public void execute_rule_equipment_attribute_false() throws JSONException , NamingException, Exception {
        AttributeValue attribute = new AttributeValue();
        attribute.setBooleanValue("0");
        List<AttributeValue> value = new ArrayList<>();
        value.add(attribute);
        Mockito.when(jdbcTemplate.query(anyString(),any(Object[].class),Matchers.<RowMapper<AttributeValue>>any())).thenReturn(value);
        executer.execute(ruleId ,fireTime,previousFireTime ,equipment, executionId , correlationId);
        PowerMockito.verifyPrivate(executer, times(1)).invoke("getEquipmentDetails", anyString(),anyString(),anyString());
        PowerMockito.verifyPrivate(executer, times(1)).invoke("getExternalIdForModel", anyString(),anyString(),anyString());
        PowerMockito.verifyPrivate(executer, times(1)).invoke("getExternalSystemURL", anyString(),anyString());
        PowerMockito.verifyPrivate(executer, times(1)).invoke("getAttributeValueForEquipment", anyString(),anyString(),anyString(),anyString(),anyString(),anyString());
        PowerMockito.verifyPrivate(executer, times(1)).invoke("getUserBpIdOfRule", anyString());
        PowerMockito.verifyPrivate(executer, times(1)).invoke("getTenantId", anyString());
        PowerMockito.verifyPrivate(executer, times(0)).invoke("getEventTypeId", anyString(),anyString());
        PowerMockito.verifyPrivate(executer, times(0)).invoke("getIndicatorMappedToAlerts", anyString(),anyString());
        PowerMockito.verifyPrivate(executer, times(0)).invoke("getIndicatorGroupMappedToAlerts", anyString(),anyString());
        PowerMockito.verifyPrivate(executer, times(0)).invoke("getPropertyForIndicator", any(), any(),any());
        Mockito.verify(alertTypeDao,times(0)).createAlert(any(),any(),any(),any(),any(),any(),any());

    }


}

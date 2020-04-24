package com.sap.iot.ain.rules.controller;



import com.sap.dsc.ac.iotae.utils.ClientSetup;
import com.sap.iot.ain.notification.services.NotificationServices;
import com.sap.iot.ain.rules.implementation.RuleExecutor;
import com.sap.iot.ain.rules.models.Equipment;
import com.sap.iot.ain.rules.services.RuleService;
import com.sap.iot.ain.template.dao.AlertTypeDao;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;


public class RunMain {


    static String  ruleId = "96b6362f276847a7ba36444bff9591fa";
    Instant fireTime = Instant.now();
    Instant previousFireTime = Instant.now().minusSeconds(60*2);
    Equipment equipment = null;
    String executionId = null;
    String correlationId= null;



    DriverManagerDataSource dataSource = new DriverManagerDataSource();

    @Spy
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

    @Spy
    ClientSetup clientSetup = new ClientSetup();

    @Spy
    AlertTypeDao alertTypeDao = new AlertTypeDao();

    @Spy
    NotificationServices notificationServices = new NotificationServices();

    @Spy
    RuleService service  = new RuleService();


    EntityManagerFactory emf = Persistence.createEntityManagerFactory("rules-persistence-test");;

    @Spy
    EntityManager em = emf.createEntityManager();

    @Spy
    @InjectMocks
    RuleExecutor executer =  new RuleExecutor();



    JSONObject json = null;

    @Before
    public void setup() throws IOException , JSONException,NamingException {
        MockitoAnnotations.initMocks(this);
        dataSource.setDriverClassName("com.sap.db.jdbc.Driver");
        dataSource.setUrl("jdbc:sap://localhost:30015?currentschema=AIN_DEV");
        dataSource.setUsername("AIN_TECH_USER");
        dataSource.setPassword("Demo123.");
        String token = "INPUT BEARER TOKEN TO CREATE ALERT";
        Mockito.doReturn(token).when(executer).getJwtTokenForIOTAE(Mockito.anyString());
    }



    @Test
    public void execute_rule() throws JSONException , NamingException {
        executer.execute(ruleId ,fireTime,previousFireTime ,equipment, executionId , correlationId);
    }
}

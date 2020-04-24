package com.sap.iot.ain.rules.implementation;

import javax.naming.NamingException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sap.iot.ain.externalidmapping.payload.SystemInfo;
import com.sap.dsc.ac.iotae.utils.ClientSetup;
import com.sap.dsc.ac.iotae.utils.IOTAeConstants;
import com.sap.iot.ain.rules.models.WSEvents;
import com.sap.iot.ain.rules.utils.RuleUtils;

@Component
public class ActionHandler {

    @Autowired
    private SystemsDAO systemsDao;

    @Autowired
    private ClientSetup clientSetup;

    private static final Logger logger = LoggerFactory.getLogger(ActionHandler.class);

    public Response createEvent(String clientID, WSEvents event, String tenantId) {
        try {
            RuleUtils utils = new RuleUtils();
            String jwt = utils.getJwtToken(tenantId);
            String authorization = "Bearer " + jwt;
            SystemInfo sysInfo = systemsDao.getSystemInfo(clientID).stream()
                    .filter(s -> IOTAeConstants.SYSTEM_NAME_EVENT.equals(s.getSystemName()))
                    .findFirst().orElse(null);
            String url = sysInfo.getURL1();
            Entity<WSEvents> iotAEEvent = Entity.entity(event, MediaType.APPLICATION_JSON);

            Response responseCreate = clientSetup.post(url, authorization, iotAEEvent, true);
            if (responseCreate.getStatus() == 201) {
            	logger.info("Alert created");
            } else {
            	logger.error("Alert creation failed: " + responseCreate.getStatus());
            }
            String output = responseCreate.readEntity(String.class);
            logger.info("Response String is: \n" + output);
            return responseCreate;
        } catch (NamingException e) {  
        	logger.error("Exception in action handler {}" + e.getMessage());
        	return Response.serverError().build();
        }
    }

}

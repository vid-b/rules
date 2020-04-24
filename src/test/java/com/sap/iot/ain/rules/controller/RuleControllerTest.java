package com.sap.iot.ain.rules.controller;


import java.io.File;
import java.io.IOException;

import javax.naming.NamingException;

import com.sap.iot.ain.rules.services.RuleService;
import com.sap.xs2.security.container.SecurityContext;
import com.sap.xs2.security.container.UserInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.quartz.SchedulerException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.iot.ain.reuse.utils.EnvironmentUtils;
import com.sap.iot.ain.rules.controller.RuleController;
import com.sap.iot.ain.rules.models.RuleWithSteps;
import com.sap.xs2.security.container.UserInfoException;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PrepareForTest({ EnvironmentUtils.class , SecurityContext.class})
public class RuleControllerTest {
	ObjectMapper mapper = new ObjectMapper();
	@Mock
	UserInfo user;


	@Spy
	private RuleService ruleService = new RuleService();

	@InjectMocks
	RuleController rulecontroller = new RuleController();

	@Test
	public void test() throws JsonParseException, JsonMappingException, IOException, UserInfoException, SchedulerException, NamingException {
		PowerMockito.mockStatic(EnvironmentUtils.class);
		PowerMockito.mockStatic(SecurityContext.class);
		PowerMockito.when(EnvironmentUtils.isCF()).thenReturn(true);
		PowerMockito.when(SecurityContext.getUserInfo()).thenReturn(user);
		File payload = new File("src/test/java/com/sap/iot/ain/rules/cron/Payload_Rule_Create.json");
		RuleWithSteps oRuleSteps = mapper.readValue(payload, RuleWithSteps.class);
		//Mockito.when(ruleService.createRule()).thenReturn(oRuleSteps)
		//rulecontroller.createRuleWithSteps(oRuleSteps);
	}

}

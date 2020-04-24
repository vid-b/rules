/*package com.sap.iot.ain.rules.cron;


import java.io.File;
import java.io.IOException;

import javax.naming.NamingException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.quartz.SchedulerException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.iot.ain.reuse.utils.EnvironmentUtils;
import com.sap.iot.ain.rules.controller.RuleControllerV1;
import com.sap.iot.ain.rules.models.RuleWithSteps;
import com.sap.xs2.security.container.UserInfoException;
import static org.mockito.Mockito.*;


@PrepareForTest(EnvironmentUtils.class)
@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
public class RuleControllerTest {
		RuleControllerV1  rulecontroller = new RuleControllerV1();
		public static File PAYLOAD = null;
		ObjectMapper mapper = new ObjectMapper();
		
		@Before
		public void setup() {
			PAYLOAD = new File("src/test/java/com/sap/iot/ain/rules/cron/Payload_Rule_Create.json");
			PowerMockito.mockStatic(EnvironmentUtils.class);
		}
		
		
		
		@Test
		public void test_rule_create_rule() throws IOException {
			when(EnvironmentUtils.isCF()).thenReturn(true);
			RuleWithSteps ruleEvent = mapper.readValue(PAYLOAD, RuleWithSteps.class);
			try {
				rulecontroller.createRuleWithSteps(ruleEvent);
			} catch (UserInfoException e) {
				e.printStackTrace();
			} catch (SchedulerException e) {
				e.printStackTrace();
			} catch (NamingException e) {
				e.printStackTrace();
			}
			//assertEquals(1, 1);
		}
}	
*/
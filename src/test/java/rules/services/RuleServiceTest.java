package rules.services;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.sap.iot.ain.rules.models.RuleAction;
import com.sap.iot.ain.rules.models.RuleWithSteps;
import com.sap.iot.ain.rules.services.RuleService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath*:testContext/persistenceConfig.xml")
public class RuleServiceTest {

	@Test
	public void extractAlertTypeIdTest() {
		String emailActionId = "sendemail";
		String alertActionId = "alerttype";
		String notificationActionId = "notification";
		String alertID = "F8526911F2114E1DB8B30DB3B0F78817";
		String alertTypeActionsParams = "{\"externalID\":\"dca.test.pdms.events:E_Temparature_Increased\","
				+ "\"alertTypeGroupID\":\"BAB8651F3F804153A8F471470DC770AA\"," + "\"alertID\":\""
				+ alertID + "\"," + "\"severity\":\"1\","
				+ "\"severityDescription\":\"Information\"," + "\"errorCode\":\"None\","
				+ "\"status\":\"New\"}";
		
		String emailActionsParams = "{\"to\":[{\"personID\":\"E67AE31620B64CE28067C889DFC3704A\"}]}";
		String notificationActionsParams = "{\"to\":[{\"type\":\"M1\",\"priority\":\"15\"}]}";
		
		List<RuleAction> ruleActionsList = new ArrayList<RuleAction>();
		RuleAction ruleActions = new RuleAction();
		ruleActions.setType("REST");
		ruleActions.setActionId(alertActionId);
		ruleActions.setDescription("Temparature_Increased");
		ruleActions.setActionParams(alertTypeActionsParams);
		ruleActionsList.add(ruleActions);
		
		ruleActions = new RuleAction();
		ruleActions.setType("EMAIL");
		ruleActions.setActionId(emailActionId);
		ruleActions.setDescription(" ");
		ruleActions.setActionParams(emailActionsParams);
		ruleActionsList.add(ruleActions);
		
		ruleActions = new RuleAction();
		ruleActions.setType("NOTIFICATION");
		ruleActions.setActionId(notificationActionId);
		ruleActions.setDescription(" ");
		ruleActions.setActionParams(notificationActionsParams);
		ruleActionsList.add(ruleActions);
		
		RuleWithSteps oRuleSteps = new RuleWithSteps();
		oRuleSteps.setRuleActions(ruleActionsList);

		RuleService rs = new RuleService();
		rs.extractAlertTypeId(oRuleSteps);
		for(RuleAction ruleAction : oRuleSteps.getRuleActions()) {
			if(ruleAction.getActionId().equals(alertActionId)) {
				//Assert.assertEquals(ruleAction.getAlertTypeId(), alertID);				
			}
			else if(ruleAction.getActionId().equals(emailActionId)) {
				//Assert.assertEquals(ruleAction.getAlertTypeId(), null);
			}
			else if(ruleAction.getActionId().equals(notificationActionId)) {
				//Assert.assertEquals(ruleAction.getAlertTypeId(), null);
			}else {
				//Assert.assertEquals(1, 0);
			}
		}
	}
}

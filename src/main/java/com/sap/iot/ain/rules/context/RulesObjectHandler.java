package com.sap.iot.ain.rules.context;

import java.io.IOException;
import java.sql.Types;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.iot.ain.reuse.Strings;
import com.sap.iot.ain.reuse.cache.CacheManager.ObjectHandler;
import com.sap.iot.ain.reuse.utils.HanaStoredProcedure;
import com.sap.iot.ain.rules.models.EmailActionParam;
import com.sap.iot.ain.rules.models.EmailRecipient;
import com.sap.iot.ain.rules.models.RuleAction;
import com.sap.iot.ain.rules.models.RuleContext;
import com.sap.iot.ain.rules.models.RuleStep;
import com.sap.iot.ain.rules.models.RuleWithSteps;
import com.sap.iot.ain.security.AuthenticatedUserDetails;
import com.sap.iot.ain.validation.utils.AINSingletonSpringBeans;

public class RulesObjectHandler implements ObjectHandler<String, RuleContext> {

	private final RuleWithSteps ruleWithSteps;

	public RulesObjectHandler(RuleWithSteps ruleWithSteps) {
		this.ruleWithSteps = ruleWithSteps;
	}

	@Override
	public RuleContext getObject() {
		RuleStoredProcedure proc = new RuleStoredProcedure();
		RuleContext rulePOSTContext = null;

		rulePOSTContext = proc.execute(ruleWithSteps);

		return rulePOSTContext;
	}

	@Override
	public String getKey() {
		return "RULE_POST_VALIDATION";
	}

	private class RuleStoredProcedure extends HanaStoredProcedure {

		private static final String IN_CLIENT = "iv_client_id";
		private static final String IN_USER_BP_ID = "iv_user_bp_id";
		private static final String IN_SCOPE = "iv_scope";
		private static final String IN_INDICATOR_IDS = "iv_indicator_ids";
		private static final String IN_PST_IDS = "iv_pst_ids";
		private static final String IN_ING_IDS = "iv_ing_ids";
		private static final String IN_SUBJECT_ID = "iv_subject_id";
		private static final String IN_USER_BP_IDS = "iv_user_bp_ids";
		private static final String IN_SUBJECT_TYPE = "iv_subject_type";

		private static final String OUT_IS_INDICATOR_IDS_EXIST = "ov_indicator_ids_exist";
		private static final String OUT_IS_PST_IDS_EXIST = "ov_pst_ids_exist";
		private static final String OUT_IS_ING_IDS_EXIST = "ov_ing_ids_exist";
		private static final String OUT_IS_SUBJECT_ID_EXIST = "ov_subject_id_exist";
		private static final String OUT_IS_USER_BP_IDS_EXIST = "ov_user_bp_ids_exist";


		public RuleStoredProcedure() {
			super(AINSingletonSpringBeans.getInstance().getJdbcTemplate(),
					"\"sap.ain.proc.rules::RuleValidation\"");
			declareParameter();
			compile();
		}

		public RuleContext execute(RuleWithSteps ruleWithSteps) {

			Map<String, Object> inputs = new HashMap<String, Object>();

			String subjectId = ruleWithSteps.ruleSubject.getSubjectID();
			String subjectType = ruleWithSteps.ruleSubject.getSubjectType();
			Set<String> indicatorIds = new HashSet<String>();
			Set<String> pstIds = new HashSet<String>();
			Set<String> ingIds = new HashSet<String>();
			Set<String> userBpIds = new HashSet<String>();
			if (ruleWithSteps.ruleSteps != null) {
				for (RuleStep steps : ruleWithSteps.ruleSteps) {
					if (!Strings.isNullOrEmpty(steps.getField1IsIndicator())
							&& steps.getField1IsIndicator().equals("X")) {
						indicatorIds.add(steps.getField1());
						if (!Strings.isNullOrEmpty(steps.getField1PST())) {
							ingIds.add(steps.getField1PST());
						}
					} else {
						if (!Strings.isNullOrEmpty(steps.getField1PST())) {
							pstIds.add(steps.getField1PST());
						}
					}
					if (!Strings.isNullOrEmpty(steps.getField2IsIndicator())
							&& steps.getField2IsIndicator().equals("X")) {
						indicatorIds.add(steps.getField2());
						if (!Strings.isNullOrEmpty(steps.getField2PST())) {
							ingIds.add(steps.getField2PST());
						}
					} else {
						if (!Strings.isNullOrEmpty(steps.getField2PST())) {
							pstIds.add(steps.getField2PST());
						}
					}
				}
			}

			if (ruleWithSteps.ruleActions != null) {
				for (RuleAction ruleAction : ruleWithSteps.ruleActions) {
					if (ruleAction.getActionId().equalsIgnoreCase("sendemail")) {
						EmailActionParam emailActionParam = null;
						try {
							emailActionParam = new ObjectMapper().readValue(
									ruleAction.getActionParams(), EmailActionParam.class);
							for (EmailRecipient emailRecipient : emailActionParam.getTo()) {
								if (!Strings.isNullOrEmpty(emailRecipient.getPersonID())) {
									userBpIds.add(emailRecipient.getPersonID());
								}
							}
						} catch (IOException e) {
							e.printStackTrace();
						}

					}
				}
			}

			inputs.put(IN_CLIENT,
					AuthenticatedUserDetails.getInstance().getUserDetails().getUserClientId());
			inputs.put(IN_USER_BP_ID,
					AuthenticatedUserDetails.getInstance().getUserDetails().getUserBpId());
			inputs.put(IN_SCOPE,
					AuthenticatedUserDetails.getInstance().getUserDetails().getScope());
			inputs.put(IN_INDICATOR_IDS, indicatorIds.stream().collect(Collectors.joining(",")));
			inputs.put(IN_PST_IDS, pstIds.stream().collect(Collectors.joining(",")));
			inputs.put(IN_ING_IDS, ingIds.stream().collect(Collectors.joining(",")));
			inputs.put(IN_SUBJECT_ID, subjectId);
			inputs.put(IN_USER_BP_IDS, userBpIds.stream().collect(Collectors.joining(",")));
			inputs.put(IN_SUBJECT_TYPE, subjectType);

			RuleContext context = new RuleContext();
			try {
				Map<String, Object> output = super.execute(inputs);

				if (output.get(OUT_IS_INDICATOR_IDS_EXIST) != null) {
					context.setOv_indicator_ids_exist(
							Boolean.parseBoolean((String) output.get(OUT_IS_INDICATOR_IDS_EXIST)));
				}
				if (output.get(OUT_IS_PST_IDS_EXIST) != null) {
					context.setOv_pst_ids_exist(
							Boolean.parseBoolean((String) output.get(OUT_IS_PST_IDS_EXIST)));
				}
				if (output.get(OUT_IS_ING_IDS_EXIST) != null) {
					context.setOv_ing_ids_exist(
							Boolean.parseBoolean((String) output.get(OUT_IS_ING_IDS_EXIST)));
				}
				if (output.get(OUT_IS_SUBJECT_ID_EXIST) != null) {
					context.setOv_subject_id_exist(Boolean
							.parseBoolean((String) output.get(OUT_IS_SUBJECT_ID_EXIST)));
				}
				if (output.get(OUT_IS_USER_BP_IDS_EXIST) != null) {
					context.setOv_user_bp_ids_exist(
							Boolean.parseBoolean((String) output.get(OUT_IS_USER_BP_IDS_EXIST)));
				}
				

			} catch (Exception e) {
				logger.debug("Exception occured while executing rule post validation procedure..");
			}

			return context;

		}

		private void declareParameter() {
			declareParameter(new SqlParameter(IN_CLIENT, Types.NVARCHAR));
			declareParameter(new SqlParameter(IN_USER_BP_ID, Types.NVARCHAR));
			declareParameter(new SqlParameter(IN_SCOPE, Types.NVARCHAR));
			declareParameter(new SqlParameter(IN_INDICATOR_IDS, Types.NVARCHAR));
			declareParameter(new SqlParameter(IN_PST_IDS, Types.NVARCHAR));
			declareParameter(new SqlParameter(IN_ING_IDS, Types.NVARCHAR));
			declareParameter(new SqlParameter(IN_SUBJECT_ID, Types.NVARCHAR));
			declareParameter(new SqlParameter(IN_USER_BP_IDS, Types.NVARCHAR));
			declareParameter(new SqlParameter(IN_SUBJECT_TYPE, Types.NVARCHAR));

			declareParameter(new SqlOutParameter(OUT_IS_INDICATOR_IDS_EXIST, Types.NVARCHAR));
			declareParameter(new SqlOutParameter(OUT_IS_PST_IDS_EXIST, Types.NVARCHAR));
			declareParameter(new SqlOutParameter(OUT_IS_ING_IDS_EXIST, Types.NVARCHAR));
			declareParameter(new SqlOutParameter(OUT_IS_SUBJECT_ID_EXIST, Types.NVARCHAR));
			declareParameter(new SqlOutParameter(OUT_IS_USER_BP_IDS_EXIST, Types.NVARCHAR));
		}
	}
}

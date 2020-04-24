package com.sap.iot.ain.rules.validation;

import java.util.ArrayList;
import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.sap.iot.ain.reuse.Strings;
import com.sap.iot.ain.reuse.cache.CacheManager;
import com.sap.iot.ain.rules.context.RulesObjectHandler;
import com.sap.iot.ain.rules.cron.Cron;
import com.sap.iot.ain.rules.cron.CronFields;
import com.sap.iot.ain.rules.cron.Operation;
import com.sap.iot.ain.rules.models.RuleAction;
import com.sap.iot.ain.rules.models.RuleContext;
import com.sap.iot.ain.rules.models.RuleEvent;
import com.sap.iot.ain.rules.models.RuleWithSteps;
import com.sap.iot.ain.security.AuthenticatedUserDetails;
import com.sap.iot.ain.validation.utils.AINSingletonSpringBeans;
import com.sap.iot.ain.validation.utils.MsgHelper;

public class RulesPOSTValidator implements ConstraintValidator<RulesPOSTValiadtions, RuleWithSteps> {

	private static final Logger logger = LoggerFactory.getLogger(RulesPOSTValidator.class);

	@Override
	public void initialize(RulesPOSTValiadtions constraintAnnotation) {
	}

	private boolean validateOnEveryValue(Cron cron, int on_min, int on_max, int every_min, int every_max, String type,
			ConstraintValidatorContext context) {

		if (cron.getOperation() == Operation.ON) {
			if (cron.getValue() != null) {
				if (!isValidValue(cron, on_min, on_max)) {
					context.buildConstraintViolationWithTemplate(MsgHelper.buildMsg("rule.event.type.range.validation",
							type, String.valueOf(on_min), String.valueOf(on_max))).addConstraintViolation();
					return false;
				}
			} else {
				context.buildConstraintViolationWithTemplate(
						MsgHelper.buildMsg("rule.event.type.range.validation.mandatory", type))
						.addConstraintViolation();
				return false;
			}
		} else if (cron.getOperation() == Operation.EVERY) {
			if (cron.getValue() != null) {
				if (!isValidValue(cron, 1, Integer.MAX_VALUE)) {
					context.buildConstraintViolationWithTemplate(MsgHelper.buildMsg("rule.event.type.range.validation",
							type, String.valueOf(every_min), String.valueOf(every_max))).addConstraintViolation();
					return false;
				}
			}
		}
		return true;
	}

	private boolean isValidValue(Cron value, int max, int min) {
		return (min <= value.getValue() && value.getValue() <= max) ? true : false;
	}

	private boolean isListNotNull(Cron[] list) {
		// TODO
		return true;
	}

	private boolean isRuleEventValid(List<RuleEvent> ruleEvents, ConstraintValidatorContext context) {
		Cron year = null, month = null, week = null, day = null, hour = null, minute = null;
		boolean isValid = true;
		for (RuleEvent rule : ruleEvents) {
			CronFields cron = rule.getCron();
			if (cron != null) {
				year = cron.getYear();
				month = cron.getMonth();
				week = cron.getDayOfTheWeek();
				day = cron.getDayOfTheMonth();
				hour = cron.getHour();
				minute = cron.getMinute();

				if (week != null && month != null) {
					context.buildConstraintViolationWithTemplate(
							MsgHelper.buildMsg("rule.event.type.validation.week.and.day")).addConstraintViolation();
					return false;
				}
				if (month != null && week != null) {
					context.buildConstraintViolationWithTemplate(
							MsgHelper.buildMsg("rule.event.type.validation.week.and.month")).addConstraintViolation();
					return false;
				}

				// Validate values
				isValid = validateOnEveryValue(year, 1970, 2099, 0, Integer.MAX_VALUE,
						MsgHelper.buildMsg("rule.event.type.validation.year"), context)
						&& validateOnEveryValue(month, 1, 12, 0, 12, "Month", context)
						&& validateOnEveryValue(week, 1, 7, 0, 7, "Week", context)
						&& validateOnEveryValue(day, 1, 31, 0, 31, "Day", context)
						&& validateOnEveryValue(hour, 0, 23, 0, 23, "Hour", context)
						&& validateOnEveryValue(minute, 0, 59, 0, 59, "Minute", context);
				if (!isValid) {
					return false;
				}

				// validate no two every

			}
			// Should not have multiple every
			// week and day can not be provided
			// month and week can not be provided

			// When every said on hour minute should be provided

		}
		return true;

	}

	@Override
	public boolean isValid(RuleWithSteps ruleWithSteps, ConstraintValidatorContext context) {

		context.disableDefaultConstraintViolation();
		boolean isValid = true;
		ValidationHelperDao validations = new ValidationHelperDao();

		// Mandatory fields validation

		if (Strings.isNullOrEmpty(ruleWithSteps.ruleSubject.getSubjectID())) {
			context.buildConstraintViolationWithTemplate(MsgHelper.buildMsg("rule.subject.id.valid"))
					.addConstraintViolation();
			return false;

		}

		// check for duplicate name

		if (Strings.isNullOrEmpty(ruleWithSteps.rule.getName())) {
			context.buildConstraintViolationWithTemplate(MsgHelper.buildMsg("rule.name.valid"))
					.addConstraintViolation();
			return false;

		}

		/*
		 * if (validations.isRuleNameValid(ruleWithSteps.rule.getName())) {
		 * context.buildConstraintViolationWithTemplate(
		 * MsgHelper.buildMsg("rule.name.valid")).addConstraintViolation(); return
		 * false;
		 * 
		 * }
		 */

		// DB validation

		CacheManager cacheManager = AINSingletonSpringBeans.getInstance().getCacheManager();
		RuleContext ruleContext = null;
		ruleContext = cacheManager.getObject(new RulesObjectHandler(ruleWithSteps));

		if (!ruleContext.isOv_indicator_ids_exist()) {
			context.buildConstraintViolationWithTemplate(MsgHelper.buildMsg("rule.indicator.id.valid"))
					.addConstraintViolation();
			return false;
		}

		if (!ruleContext.isOv_pst_ids_exist()) {
			context.buildConstraintViolationWithTemplate(MsgHelper.buildMsg("rule.pst.id.valid"))
					.addConstraintViolation();
			return false;
		}

		if (!ruleContext.isOv_ing_ids_exist()) {
			context.buildConstraintViolationWithTemplate(MsgHelper.buildMsg("rule.ing.id.valid"))
					.addConstraintViolation();
			return false;
		}

		if (!ruleContext.isOv_subject_id_exist()) {
			context.buildConstraintViolationWithTemplate(MsgHelper.buildMsg("rule.subject.id.valid"))
					.addConstraintViolation();
			return false;
		}

		if (!ruleContext.isOv_user_bp_ids_exist()) {
			context.buildConstraintViolationWithTemplate(MsgHelper.buildMsg("rule.userbp.id.valid"))
					.addConstraintViolation();
			return false;
		}

		// YET TO COMPLETE IMPLEMENT
		// isValid = isRuleEventValid(ruleWithSteps.getRuleEvents(), context);

		return isValid;

	}

}

package com.sap.iot.ain.rules.validation;



import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraintvalidation.SupportedValidationTarget;
import javax.validation.constraintvalidation.ValidationTarget;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.iot.ain.validation.utils.MsgHelper;

@SupportedValidationTarget(ValidationTarget.PARAMETERS)
public class RuleAlertTypeValidator implements ConstraintValidator<RuleAlertTypeValidations, Object[]> {
	
	private static final Logger logger = LoggerFactory.getLogger(RulesPOSTValidator.class);
	@Override
	public void initialize(RuleAlertTypeValidations constraintAnnotation) {
		
	}
	
	@Override
	public boolean isValid(Object[] values, ConstraintValidatorContext context) {
	context.disableDefaultConstraintViolation();
	ValidationHelperDao validations = new ValidationHelperDao();
	String ruleId = (String) values[0];
	//check if alertype present
	
	if (!validations.isRuleAlertTypePresent(ruleId)) {
		context.buildConstraintViolationWithTemplate(
				MsgHelper.buildMsg("alertType.noexists")).addConstraintViolation();
		return false;

	}

	/*if (!validations.isAlertExternalIDValid(ruleId)) {
		context.buildConstraintViolationWithTemplate(
				MsgHelper.buildMsg("rule.alerttype.externalid.valid")).addConstraintViolation();
		return false;

	}*/
	
	return true;
	}
	
	

}

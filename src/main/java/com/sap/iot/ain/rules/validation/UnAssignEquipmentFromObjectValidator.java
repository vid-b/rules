package com.sap.iot.ain.rules.validation;



import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraintvalidation.SupportedValidationTarget;
import javax.validation.constraintvalidation.ValidationTarget;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.sap.iot.ain.rules.models.RuleEquipmentMapping;

import com.sap.iot.ain.validation.utils.MsgHelper;

@Component
@SupportedValidationTarget(ValidationTarget.PARAMETERS)
public class UnAssignEquipmentFromObjectValidator implements ConstraintValidator<UnAssignEquipmentFromObjectValidation, Object[]> {

	private static final Logger logger = LoggerFactory.getLogger(AssignEquipmentToObjectValidator.class);



	@Override
	public void initialize(UnAssignEquipmentFromObjectValidation constraintAnnotation) {
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
	}

	@Override
	public boolean isValid(Object[] values, ConstraintValidatorContext context) {
		context.disableDefaultConstraintViolation();
		ValidationHelperDao validations = new ValidationHelperDao();
		String ruleId = (String) values[0];
		RuleEquipmentMapping businessObjects = (RuleEquipmentMapping) values[1];
		
		if(!validations.checkObjectAuthorization(businessObjects.getEquipmentIds(), "EQU")) {
			context.buildConstraintViolationWithTemplate(
					MsgHelper.buildMsg("rule.object.valid")).addConstraintViolation();
			return false;
		} 
		
		if(!validations.isInsModValid(businessObjects.getSubjectId(), ruleId)) {
			context.buildConstraintViolationWithTemplate(
					MsgHelper.buildMsg("rule.subject.valid")).addConstraintViolation();
			return false;
		} 
		
		
	
		return true;

	}
}

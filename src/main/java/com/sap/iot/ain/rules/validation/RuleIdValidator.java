package com.sap.iot.ain.rules.validation;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraintvalidation.SupportedValidationTarget;
import javax.validation.constraintvalidation.ValidationTarget;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.sap.iot.ain.rules.models.Rule;
import com.sap.iot.ain.rules.services.RuleService;

@Component
@SupportedValidationTarget(ValidationTarget.PARAMETERS)
public class RuleIdValidator implements ConstraintValidator<RuleIdValidations, Object[]> {

	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private RuleService ruleService;
	
	@Override
	public void initialize(RuleIdValidations arg0) {
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
	}

	@Override
	public boolean isValid(Object[] values, ConstraintValidatorContext context) {
		context.disableDefaultConstraintViolation();

		String ruleId = (String) values[0];

		Rule rule = ruleService.getRule(ruleId, em);

		if (rule == null) {
			context.buildConstraintViolationWithTemplate("rule.invalid.id")
					.addConstraintViolation();
			return false;
		}
		return true;
	}


}


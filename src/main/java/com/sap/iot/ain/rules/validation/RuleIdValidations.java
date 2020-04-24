package com.sap.iot.ain.rules.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RuleIdValidator.class)
@Documented
public @interface RuleIdValidations {
	
	String message() default "reuse.invalid.id";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}

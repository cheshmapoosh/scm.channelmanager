package ir.daneshrefah.scm.common.validation;

import ir.daneshrefah.scm.common.validation.validator.NumericValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NumericValidator.class)
public @interface Numeric {
    String message() default "must be numeric";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

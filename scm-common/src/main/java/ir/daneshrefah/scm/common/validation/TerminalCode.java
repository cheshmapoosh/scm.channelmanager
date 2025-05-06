package ir.daneshrefah.scm.common.validation;

import ir.daneshrefah.scm.common.validation.validator.PasswordValidator;
import ir.daneshrefah.scm.common.validation.validator.TerminalCodeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TerminalCodeValidator.class)
public @interface TerminalCode {
    String message() default "must not be blank if present";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

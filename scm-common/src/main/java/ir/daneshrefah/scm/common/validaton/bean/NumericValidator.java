package ir.daneshrefah.scm.common.validaton.bean;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class NumericValidator implements ConstraintValidator<Numeric, Object> {
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        return !Objects.isNull(value) && StringUtils.isNumeric(String.valueOf(value));
    }
}

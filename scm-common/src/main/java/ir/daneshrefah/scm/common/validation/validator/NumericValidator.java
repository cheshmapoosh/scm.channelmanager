package ir.daneshrefah.scm.common.validation.validator;

import ir.daneshrefah.scm.common.validation.Numeric;
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

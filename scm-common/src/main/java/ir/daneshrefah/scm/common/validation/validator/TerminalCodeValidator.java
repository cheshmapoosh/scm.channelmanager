package ir.daneshrefah.scm.common.validation.validator;

import ir.daneshrefah.scm.common.constant.TerminalType;
import ir.daneshrefah.scm.common.validation.TerminalCode;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

public class TerminalCodeValidator implements ConstraintValidator<TerminalCode, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.isNotBlank(value)) {
            return TerminalType.fromCode(value).isPresent();
        }
        return true;
    }
}

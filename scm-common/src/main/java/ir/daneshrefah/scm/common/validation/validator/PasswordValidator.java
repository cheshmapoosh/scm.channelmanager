package ir.daneshrefah.scm.common.validation.validator;

import ir.daneshrefah.scm.common.validation.Password;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

public class PasswordValidator implements ConstraintValidator<Password, String> {
    @Override
    public boolean isValid(String inputPassword, ConstraintValidatorContext constraintValidatorContext) {
        if (StringUtils.isNotBlank(inputPassword)) {
            return
                    (inputPassword.length() >= 8
                     && !StringUtils.isNumeric(inputPassword)
                     && !StringUtils.isAlpha(inputPassword));
        }
        return true;
    }
}

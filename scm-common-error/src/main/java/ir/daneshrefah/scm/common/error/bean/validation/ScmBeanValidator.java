package ir.daneshrefah.scm.common.error.bean.validation;

import ir.daneshrefah.scm.common.model.error.Error;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import jakarta.validation.*;

import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ScmBeanValidator implements AutoCloseable {

    private final Validator validator;
    private final MessageInterpolator interpolator;
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();

    public ScmBeanValidator() {
        this.validator = factory.getValidator();
        this.interpolator = factory.getMessageInterpolator();
    }

    @Override
    public void close() {
        factory.close();
    }

    public <T> void validateBean(T bean) {
        Set<ConstraintViolation<T>> violations = validator.validate(bean);
        List<Error> errors = violations.stream().map(v -> {
            String msgFa = interpolator.interpolate(v.getMessageTemplate(),
                    new MessageInterpolatorContext<>(v)
                    , new Locale("fa"));

            String msgEn = interpolator.interpolate(v.getMessageTemplate(),
                    new MessageInterpolatorContext<>(v)
                    , Locale.ENGLISH);

            return new Error(v.getPropertyPath().toString(), 1000, msgEn, msgFa, MessageStatus.SC_ERROR_VALIDATION, null);
        }).toList();
        if (!errors.isEmpty()) {
            throw new ScmBeanValidationException(errors);
        }
    }

}

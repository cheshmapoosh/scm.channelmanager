package ir.daneshrefah.scm.common.error.bean.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.MessageInterpolator;
import jakarta.validation.ValidationException;
import jakarta.validation.metadata.ConstraintDescriptor;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MessageInterpolatorContext<T> implements MessageInterpolator.Context {
    private final ConstraintViolation<T> constraintViolation;

    @Override
    public ConstraintDescriptor<?> getConstraintDescriptor() {
        return constraintViolation.getConstraintDescriptor();
    }

    @Override
    public Object getValidatedValue() {
        return constraintViolation.getConstraintDescriptor();
    }

    @Override
    public <C> C unwrap(Class<C> type) {
        if (type.isInstance(this)) {
            return type.cast(this);
        }
        throw new ValidationException("Type " + type + " not supported for unwrap.");
    }
}

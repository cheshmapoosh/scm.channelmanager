package ir.daneshrefah.scm.common.exception;

import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.ExceptionInformationBuilder;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import jakarta.validation.ConstraintViolation;
import lombok.Getter;

import java.util.Set;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-14
 */
@Getter
public class InvalidInputException extends AbstractValidationException {

    private final Set<ConstraintViolation<Object>> violations;
    public InvalidInputException(Set<ConstraintViolation<Object>> violations) {
        super(violations.toString(), violations + " are invalid.");
        this.violations = violations;
    }

    public InvalidInputException(String source) {
        super(source, source + " is invalid.");
        this.violations = null;
    }

    @Override
    public ExceptionInformation getExceptionInformation() {
        return ExceptionInformationBuilder
                .createInstance()
                .buildWithStatus(MessageStatus.SC_ERROR_VALIDATION);
    }
}

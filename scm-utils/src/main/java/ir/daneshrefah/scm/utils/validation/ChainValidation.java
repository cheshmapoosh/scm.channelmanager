package ir.daneshrefah.scm.utils.validation;

import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.utils.string.StringUtils;

import java.util.Objects;

public class ChainValidation {
    private boolean breakChain = false;
    private Object source;
    private String fieldName;

    public static ChainValidation crateValidator(Object source, String fieldName) {
        ChainValidation chainValidation = new ChainValidation();
        chainValidation.source = source;
        chainValidation.fieldName = fieldName;
        return chainValidation;
    }

    public ChainValidation checkNull() {
        if (!breakChain) {
            ValidationUtils.checkNull(source, () -> new InvalidInputException(fieldName));
        }
        return this;
    }

    public ChainValidation checkBlank() {
        if (!breakChain) {
            ValidationUtils.checkBlankString(String.valueOf(source), () -> new InvalidInputException(fieldName));
        }
        return this;
    }

    public ChainValidation checkNumeral() {
        if (!breakChain) {
            ValidationUtils.checkNumericInput(String.valueOf(source), () -> new InvalidInputException(fieldName));
        }
        return this;
    }

    public ChainValidation breakCheckIfNull() {
        if (Objects.isNull(source)) {
            breakChain = true;
        }
        return this;
    }

    public ChainValidation breakCheckIfNullOrBlank() {
        if (Objects.isNull(source) || StringUtils.isNotBlank(String.valueOf(source))) {
            breakChain = true;
        }
        return this;
    }

    public ChainValidation breakCheckIfNotNumeric() {
        if (!StringUtils.isNumeric(String.valueOf(source))) {
            breakChain = true;
        }
        return this;
    }

    public ChainValidation checkFunction(ValidationFunction validationFunction) {
        if (!breakChain && !validationFunction.isValid(source)) {
            throw new InvalidInputException(fieldName);
        }
        return this;
    }



    @FunctionalInterface
    public interface ValidationFunction {
        boolean isValid(Object input);
    }


}

package ir.daneshrefah.scm.common.error.bean.validation;

import ir.daneshrefah.scm.common.exception.ScmException;
import ir.daneshrefah.scm.common.model.error.Error;
import lombok.Getter;

import java.util.List;

public class ScmBeanValidationException extends ScmException {
    private static final String SCM_BEAN_VALIDATION_CODE = "SCM.BEAN.VALIDATION.1000";
    @Getter
    private final List<Error> errors;

    public ScmBeanValidationException(List<Error> errors) {
        super(SCM_BEAN_VALIDATION_CODE, "bean validation");
        this.errors = errors;
    }
}

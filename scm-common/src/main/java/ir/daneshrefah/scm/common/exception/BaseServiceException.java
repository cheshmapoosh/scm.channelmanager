package ir.daneshrefah.scm.common.exception;

import ir.daneshrefah.scm.common.error.spec.AbstractBaseException;
import ir.daneshrefah.scm.common.error.spec.ExceptionSourceAware;
import ir.daneshrefah.scm.common.model.service.ScmService;
import lombok.Getter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-27
 */
public abstract class BaseServiceException extends AbstractBaseException implements ExceptionSourceAware {

    @Getter
    String serviceCode;

    public BaseServiceException(String message, Throwable cause, ScmService service) {
        this(message, cause, service.getCode());
    }

    public BaseServiceException(String message, Throwable cause, String serviceCode) {
        super(message, cause);
        this.serviceCode = serviceCode;
    }

    @Override
    public String getSource() {
        return serviceCode;
    }

}

package ir.daneshrefah.scm.common.exception;

import ir.daneshrefah.scm.common.model.service.Service;
import lombok.Getter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-27
 */
@Getter
public class ServiceExecutionException extends BaseServiceException {

    private String errorCode;

    public ServiceExecutionException(Service service, String errorCode, String message) {
        this(service, errorCode, message, null);
    }

    public ServiceExecutionException(Service service, String errorCode, String message, Throwable cause) {
        super(message, cause, service);
        this.errorCode = errorCode;
    }

}

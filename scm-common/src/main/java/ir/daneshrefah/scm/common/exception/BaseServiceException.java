package ir.daneshrefah.scm.common.exception;

import ir.daneshrefah.scm.common.model.service.Service;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-27
 */
public abstract class BaseServiceException extends BaseException implements ErrorCodeAwareException {

    String serviceCode;

    public BaseServiceException(String message, Throwable cause, Service service) {
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

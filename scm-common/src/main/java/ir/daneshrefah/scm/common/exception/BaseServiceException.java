package ir.daneshrefah.scm.common.exception;

import ir.daneshrefah.scm.common.model.service.Service;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-27
 */
public abstract class BaseServiceException extends BaseException {

    Service service;

    public BaseServiceException(String message, Throwable cause, Service service) {
        super(message, cause);
        this.service = service;
    }

    @Override
    public String getSource() {
        return service.getCode();
    }

}

package ir.daneshrefah.scm.common.exception;

import ir.daneshrefah.scm.common.model.service.Service;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-27
 */
public class DisableServiceExecutionException extends BaseServiceException {

    public DisableServiceExecutionException(Service service) {
        this("service is disabled: " + service.getCode(), service);
    }

    public DisableServiceExecutionException(String message, Service service) {
        super(message, null, service);
    }

}

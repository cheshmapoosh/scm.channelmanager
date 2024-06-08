package ir.daneshrefah.scm.plugin.api.exception;

import ir.daneshrefah.scm.common.exception.BaseServiceException;
import ir.daneshrefah.scm.plugin.api.model.service.java.JavaService;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-01-17
 */
public abstract class AbstractJavaServiceException extends BaseServiceException {

    public AbstractJavaServiceException(String message, Throwable cause, JavaService service) {
        super(message, cause, service);
    }

}

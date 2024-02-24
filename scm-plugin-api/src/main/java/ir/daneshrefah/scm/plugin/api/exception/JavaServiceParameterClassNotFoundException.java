package ir.daneshrefah.scm.plugin.api.exception;

import ir.daneshrefah.scm.common.model.error.ErrorCodes;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.plugin.api.model.service.java.JavaService;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-24
 */
public class JavaServiceParameterClassNotFoundException extends AbstractJavaServiceException {

    public JavaServiceParameterClassNotFoundException(JavaService service, String parameterName, Throwable cause) {
        super("service [" + service.getCode() + "] , parameter [" + parameterName + "] class not found.", cause, service);
    }

    @Override
    public int getErrorCode() {
        return ErrorCodes.ERROR_CODE_VALIDATION_SERVICE_PARAMETER_CLASS_NOT_FOUND;
    }

    @Override
    public MessageStatus getStatus() {
        return MessageStatus.SC_ERROR_SYSTEM;
    }

}

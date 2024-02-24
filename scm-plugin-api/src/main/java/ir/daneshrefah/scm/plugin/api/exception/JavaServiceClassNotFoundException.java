package ir.daneshrefah.scm.plugin.api.exception;

import ir.daneshrefah.scm.common.model.error.ErrorCodes;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.plugin.api.model.service.java.JavaService;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-01-17
 */
public class JavaServiceClassNotFoundException extends AbstractJavaServiceException {

    public JavaServiceClassNotFoundException(Throwable cause, JavaService service) {
        super("error load java service class.", cause, service);
    }

    @Override
    public int getErrorCode() {
        return ErrorCodes.ERROR_CODE_JAVA_SERVICE_CLASS_NOT_FOUND;
    }

    @Override
    public MessageStatus getStatus() {
        return MessageStatus.SC_ERROR_SYSTEM;
    }

}

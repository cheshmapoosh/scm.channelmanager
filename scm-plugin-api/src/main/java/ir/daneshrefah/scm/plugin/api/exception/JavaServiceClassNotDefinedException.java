package ir.daneshrefah.scm.plugin.api.exception;

import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.ExceptionInformationBuilder;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.plugin.api.model.service.java.JavaService;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-01-17
 */
public class JavaServiceClassNotDefinedException extends AbstractJavaServiceException {

    public JavaServiceClassNotDefinedException(String serviceCode, String message) {
        this(serviceCode, null, message);
    }

    public JavaServiceClassNotDefinedException(JavaService service, String message) {
        this(service, null, message);
    }

    public JavaServiceClassNotDefinedException(JavaService service, Throwable cause) {
        this(service, cause, "java service class not found: " + service.getJavaImplementationClassName());
    }

    public JavaServiceClassNotDefinedException(String serviceCode, String javaImplementationClassName, Throwable cause) {
        this(serviceCode, cause, "java service class not found: " + javaImplementationClassName);
    }

    public JavaServiceClassNotDefinedException(JavaService service, Throwable cause, String message) {
        super(message, cause, service);
    }

    public JavaServiceClassNotDefinedException(String serviceCode, Throwable cause, String message) {
        super(message, cause, serviceCode);
    }

    @Override
    public ExceptionInformation getExceptionInformation() {
        return ExceptionInformationBuilder
                .createInstance()
                .buildWithStatus(MessageStatus.SC_ERROR_SYSTEM);
    }
}

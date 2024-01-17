package ir.daneshrefah.scm.plugin.api.exception;

import ir.daneshrefah.scm.plugin.api.model.service.java.JavaService;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-01-17
 */
public class JavaServiceClassNotDefinedException extends AbstractJavaServiceException {

    public JavaServiceClassNotDefinedException(JavaService service) {
        super("java service class not found: " + service.getJavaImplementationClassName(), null, service);
    }

}

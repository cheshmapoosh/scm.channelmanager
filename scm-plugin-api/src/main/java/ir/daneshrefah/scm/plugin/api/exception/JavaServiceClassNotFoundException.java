package ir.daneshrefah.scm.plugin.api.exception;

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

}

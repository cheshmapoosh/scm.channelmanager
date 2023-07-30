package ir.daneshrefah.scm.plugin.api.exception;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-29
 */
public abstract class ServiceComponentException extends BaseException {
    public ServiceComponentException(String correlationId, String source, String code, String message) {
        super(correlationId, source, code, message);
    }

}

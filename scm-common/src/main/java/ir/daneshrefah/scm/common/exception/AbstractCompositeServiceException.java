package ir.daneshrefah.scm.common.exception;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-03-26
 */
public abstract class AbstractCompositeServiceException extends BaseServiceException implements ErrorCodeAwareException {

    public AbstractCompositeServiceException(String message, Throwable cause, String serviceCode) {
        super(message, cause, serviceCode);
    }

}

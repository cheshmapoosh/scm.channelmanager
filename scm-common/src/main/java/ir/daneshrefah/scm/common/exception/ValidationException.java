package ir.daneshrefah.scm.common.exception;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-09
 */
public class ValidationException extends BaseException {

    public ValidationException(String errorCode, String source) {
        super(errorCode, source);
    }

}

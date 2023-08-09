package ir.daneshrefah.scm.plugin.api.exception;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-09
 */
public class ValidationException extends BaseException {

    private String errorCode;
    private Object source;

    public ValidationException(Object source, String errorCode, String message) {
        super(message);
        this.source = source;
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public Object getSource() {
        return source;
    }

    @Override
    public Object getSourceCode() {
        return source;
    }
}

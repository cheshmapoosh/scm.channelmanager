package ir.daneshrefah.scm.common.exception;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-14
 */
public abstract class AbstractValidationException extends BaseException {

    private final String source;

    public AbstractValidationException(String source, String message) {
        this(source, message, null);
    }

    public AbstractValidationException(String source, String message, Throwable cause) {
        super(message, cause);
        this.source = source;
    }

    @Override
    public String getSource() {
        return source;
    }

    public abstract int getErrorCode();

}

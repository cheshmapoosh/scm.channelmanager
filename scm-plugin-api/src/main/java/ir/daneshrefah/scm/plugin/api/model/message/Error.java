package ir.daneshrefah.scm.plugin.api.model.message;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-23
 */
public class Error {
    private String code;
    private String message;
    private Object source;
    private String sourceErrorCode;
    private String sourceErrorMessage;
    private Exception exception;

    public Error(String code, String message, Object source, String sourceErrorCode, String sourceErrorMessage, Exception exception) {
        this.code = code;
        this.message = message;
        this.source = source;
        this.sourceErrorCode = sourceErrorCode;
        this.sourceErrorMessage = sourceErrorMessage;
        this.exception = exception;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Object getSource() {
        return source;
    }

    public String getSourceErrorCode() {
        return sourceErrorCode;
    }

    public Exception getException() {
        return exception;
    }

    public String getSourceErrorMessage() {
        return sourceErrorMessage;
    }
}

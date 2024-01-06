package ir.daneshrefah.scm.common.exception;

import lombok.Getter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-26
 */
@Getter
public abstract class BaseException extends RuntimeException {

    public static final String DEFAULT_ERROR_URI = null;

    private final String errorCode;
    private String uri;
    private String source;


    protected BaseException(String errorCode, String source) {
        this(errorCode, DEFAULT_ERROR_URI, source);
    }

    protected BaseException(String errorCode, String uri, String source) {
        super("Error Code: " + errorCode);
        this.errorCode = errorCode;
        this.uri = uri;
        this.source = source;
    }

    public String getErrorDetails() {
        return errorCode;
    }
}

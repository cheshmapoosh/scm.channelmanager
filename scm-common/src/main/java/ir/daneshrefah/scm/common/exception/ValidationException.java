package ir.daneshrefah.scm.common.exception;

import lombok.Getter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-09
 */
@Getter
public class ValidationException extends BaseException {

    private String source;
    private Integer errorCode;
    public ValidationException(String source, Integer errorCode, String message) {
        this(source, errorCode, message, null);
    }

    public ValidationException(String source, Integer errorCode, String message, Throwable cause) {
        super(message, cause);
        this.source = source;
        this.errorCode = errorCode;
    }

}

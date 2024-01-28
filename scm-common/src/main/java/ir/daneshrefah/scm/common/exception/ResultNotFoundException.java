package ir.daneshrefah.scm.common.exception;

import lombok.Getter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-28
 */
@Getter
public class ResultNotFoundException extends BaseException {

    private String source;
    private Integer errorCode;
    public ResultNotFoundException(String source, Integer errorCode, String message) {
        this(source, errorCode, message, null);
    }

    public ResultNotFoundException(String source, Integer errorCode, String message, Throwable cause) {
        super(message, cause);
        this.source = source;
        this.errorCode = errorCode;
    }

}

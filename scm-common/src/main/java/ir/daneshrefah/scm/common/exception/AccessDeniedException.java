package ir.daneshrefah.scm.common.exception;

import lombok.Getter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-16
 */
@Getter
public class AccessDeniedException extends BaseException {

    private final String source;
    private final Integer errorCode;

    public AccessDeniedException(String source, Integer errorCode, String message) {
        super(message, null);
        this.source = source;
        this.errorCode = errorCode;
    }

}

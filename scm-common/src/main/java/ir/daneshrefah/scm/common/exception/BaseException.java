package ir.daneshrefah.scm.common.exception;

import lombok.Getter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-26
 */
public abstract class BaseException extends RuntimeException {

    public BaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public static final String DEFAULT_ERROR_URI = null;

}

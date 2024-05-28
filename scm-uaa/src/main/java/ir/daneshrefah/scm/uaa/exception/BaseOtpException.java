package ir.daneshrefah.scm.uaa.exception;

import ir.daneshrefah.scm.common.exception.BaseException;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-21
 */
public abstract class BaseOtpException extends BaseException {


    public BaseOtpException(String message, Throwable cause) {
        super(message, cause);
    }
}

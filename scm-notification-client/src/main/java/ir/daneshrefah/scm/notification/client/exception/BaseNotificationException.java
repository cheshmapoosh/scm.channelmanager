package ir.daneshrefah.scm.notification.client.exception;

import ir.daneshrefah.scm.common.exception.BaseException;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-05
 */
public abstract class BaseNotificationException extends BaseException {

    public BaseNotificationException(String message, Throwable cause) {
        super(message, cause);
    }

}

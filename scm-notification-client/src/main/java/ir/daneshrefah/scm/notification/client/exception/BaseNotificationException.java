package ir.daneshrefah.scm.notification.client.exception;

import ir.daneshrefah.scm.common.exception.BaseException;
import ir.daneshrefah.scm.common.exception.ErrorCodeAwareException;
import ir.daneshrefah.scm.common.model.message.MessageStatus;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-05
 */
public abstract class BaseNotificationException extends BaseException implements ErrorCodeAwareException {

    public BaseNotificationException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public MessageStatus getStatus() {
        return MessageStatus.SC_ERROR_SYSTEM;
    }

}

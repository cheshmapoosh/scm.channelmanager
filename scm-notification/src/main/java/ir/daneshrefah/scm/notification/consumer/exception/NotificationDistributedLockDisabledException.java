package ir.daneshrefah.scm.notification.consumer.exception;

import ir.daneshrefah.scm.common.exception.BaseException;
import ir.daneshrefah.scm.common.exception.ErrorCodeAwareException;
import ir.daneshrefah.scm.common.model.message.MessageStatus;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_NOTIFICATION_CONCURRENT_EXCEPTION;
import static ir.daneshrefah.scm.common.model.message.MessageStatus.SC_ERROR_SYSTEM;

public class NotificationDistributedLockDisabledException extends BaseException implements ErrorCodeAwareException {

    public NotificationDistributedLockDisabledException(String message) {
        super(message, null);
    }

    @Override
    public int getErrorCode() {
        return ERROR_CODE_NOTIFICATION_CONCURRENT_EXCEPTION;
    }

    @Override
    public String getSource() {
        return getMessage();
    }

    @Override
    public MessageStatus getStatus() {
        return SC_ERROR_SYSTEM;
    }

}

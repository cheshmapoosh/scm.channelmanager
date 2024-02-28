package ir.daneshrefah.scm.notification.exception;

import ir.daneshrefah.scm.common.exception.BaseException;


public class NotificationExecutionJobException extends BaseException {

    private final String message;
    public NotificationExecutionJobException(String message) {
        super(message, null);
        this.message = message;
    }

    @Override
    public String getSource() {
        return message;
    }


}

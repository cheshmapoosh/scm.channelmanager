package ir.daneshrefah.scm.notification.client.exception;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_NOTIFICATION_BODY_PROCESSOR_NOT_FOUND;

public class NotificationBodyProcessorDoesNotExistsException extends BaseNotificationException{

    private static final String MSG = "does not exists any body processor";
    public NotificationBodyProcessorDoesNotExistsException() {
        super(MSG, null);
    }

    @Override
    public int getErrorCode() {
        return ERROR_CODE_NOTIFICATION_BODY_PROCESSOR_NOT_FOUND;
    }

    @Override
    public String getSource() {
        return MSG;
    }
}

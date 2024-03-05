package ir.daneshrefah.scm.notification.client.exception;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_NOTIFICATION_BODY_PROCESSING_EXCEPTION;

public class NotificationBodyProcessException extends BaseNotificationException{

    private final String templateCode;
    public NotificationBodyProcessException(String templateCode) {
        super("notification body could not process for template code : " + templateCode, null);
        this.templateCode = templateCode;
    }

    @Override
    public int getErrorCode() {
        return ERROR_CODE_NOTIFICATION_BODY_PROCESSING_EXCEPTION;
    }

    @Override
    public String getSource() {
        return templateCode;
    }
}

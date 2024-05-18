package ir.daneshrefah.scm.notification.client.exception;

import ir.daneshrefah.scm.common.model.notification.NotificationRequest;

public class NotificationBodyProcessException extends BaseNotificationException{

    private final String templateCode;
    public NotificationBodyProcessException(NotificationRequest request, String templateCode) {
        super(request, "notification body could not process for template code : " + templateCode, null);
        this.templateCode = templateCode;
    }

    @Override
    public String getSource() {
        return templateCode;
    }
}

package ir.daneshrefah.scm.notification.client.exception;

import ir.daneshrefah.scm.common.model.notification.NotificationRequest;

public class NotFoundSupportedBodyProcessorException extends BaseNotificationException {
    private final String templateCode;

    public NotFoundSupportedBodyProcessorException(NotificationRequest request, String templateCode) {
        super(request, "notification body processor not found for template code : " + templateCode, null);
        this.templateCode = templateCode;
    }

    @Override
    public String getSource() {
        return templateCode;
    }

}

package ir.daneshrefah.scm.notification.client.exception;

import ir.daneshrefah.scm.common.model.notification.NotificationRequest;

public class NotificationTemplateNotFoundException extends BaseNotificationException{

    private final String requestedTemplateCode;
    public NotificationTemplateNotFoundException(NotificationRequest request, String requestedTemplateCode) {
        super(request, "No notification template found for : " + requestedTemplateCode, null);
        this.requestedTemplateCode = requestedTemplateCode;
    }

    @Override
    public String getSource() {
        return requestedTemplateCode;
    }
}

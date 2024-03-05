package ir.daneshrefah.scm.notification.client.exception;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_NOTIFICATION_TEMPLATE_NOT_FOUND;

public class NotificationTemplateNotFoundException extends BaseNotificationException{

    private final String requestedTemplateCode;
    public NotificationTemplateNotFoundException(String requestedTemplateCode) {
        super("No notification template found for : " + requestedTemplateCode, null);
        this.requestedTemplateCode = requestedTemplateCode;
    }

    @Override
    public int getErrorCode() {
        return ERROR_CODE_NOTIFICATION_TEMPLATE_NOT_FOUND;
    }

    @Override
    public String getSource() {
        return requestedTemplateCode;
    }
}

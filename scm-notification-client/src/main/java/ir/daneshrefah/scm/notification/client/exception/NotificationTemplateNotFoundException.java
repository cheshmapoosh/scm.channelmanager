package ir.daneshrefah.scm.notification.client.exception;

public class NotificationTemplateNotFoundException extends BaseNotificationException{

    private final String requestedTemplateCode;
    public NotificationTemplateNotFoundException(String requestedTemplateCode) {
        super("No notification template found for : " + requestedTemplateCode, null);
        this.requestedTemplateCode = requestedTemplateCode;
    }

    @Override
    public String getSource() {
        return requestedTemplateCode;
    }
}

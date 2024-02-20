package ir.daneshrefah.scm.notification.client.exception;

public class NotificationBodyProcessException extends BaseNotificationException{

    private final String templateCode;
    public NotificationBodyProcessException(String templateCode) {
        super("notification body could not process for template code : " + templateCode, null);
        this.templateCode = templateCode;
    }

    @Override
    public String getSource() {
        return templateCode;
    }
}

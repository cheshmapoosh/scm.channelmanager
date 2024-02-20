package ir.daneshrefah.scm.notification.client.exception;

public class NotificationBodyProcessorDoesNotExistsException extends BaseNotificationException{

    public NotificationBodyProcessorDoesNotExistsException() {
        super("does not exists any body processor", null);
    }

    @Override
    public String getSource() {
        return null;
    }
}

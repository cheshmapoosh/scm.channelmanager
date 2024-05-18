package ir.daneshrefah.scm.notification.client.exception;

import ir.daneshrefah.scm.common.model.notification.NotificationRequest;

public class NotificationBodyProcessorDoesNotExistsException extends BaseNotificationException{

    private static final String MSG = "does not exists any body processor";
    public NotificationBodyProcessorDoesNotExistsException(NotificationRequest request) {
        super(request, MSG, null);
    }

    @Override
    public String getSource() {
        return MSG;
    }
}

package ir.daneshrefah.scm.notification.client.exception;

import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.ExceptionInformationBuilder;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
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

    @Override
    public ExceptionInformation getExceptionInformation() {
        return ExceptionInformationBuilder
                .createInstance()
                .buildWithStatus(MessageStatus.SC_ERROR_VALIDATION);
    }
}

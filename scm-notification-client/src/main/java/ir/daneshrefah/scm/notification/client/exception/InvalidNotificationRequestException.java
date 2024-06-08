package ir.daneshrefah.scm.notification.client.exception;

import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.ExceptionInformationBuilder;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.common.model.notification.NotificationRequest;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-05
 */
public class InvalidNotificationRequestException extends BaseNotificationException {

    private final String property;

    public InvalidNotificationRequestException(NotificationRequest request, String property) {
        super(request, property + " is empty", null);
        this.property =property;
    }

    @Override
    public ExceptionInformation getExceptionInformation() {
        return ExceptionInformationBuilder
                .createInstance()
                .defineMessageParameter("property",property)
                .buildWithStatus(MessageStatus.SC_ERROR_VALIDATION);
    }
}

package ir.daneshrefah.scm.notification.client.exception;

import ir.daneshrefah.scm.common.model.notification.NotificationRequest;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-05
 */
public class EmptyNotificationRequestException extends BaseNotificationException {

    public EmptyNotificationRequestException(NotificationRequest request, String property) {
        super(request, property + " is empty", null);
    }

}

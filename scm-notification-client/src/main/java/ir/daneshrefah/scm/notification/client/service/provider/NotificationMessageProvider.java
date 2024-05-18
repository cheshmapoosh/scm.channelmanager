package ir.daneshrefah.scm.notification.client.service.provider;

import ir.daneshrefah.scm.common.model.notification.NotificationMessage;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-18
 */
public interface NotificationMessageProvider {

    void send(NotificationMessage notificationMessage);

    boolean supports(NotificationMessage notificationMessage);

}

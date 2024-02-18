package ir.daneshrefah.scm.notification.service.provider;


import ir.daneshrefah.scm.common.model.notification.Notification;
import ir.daneshrefah.scm.common.model.notification.NotificationMedia;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-30
 */
public interface NotificationProvider {

    void send(Notification notification);

    NotificationMedia getType();

}

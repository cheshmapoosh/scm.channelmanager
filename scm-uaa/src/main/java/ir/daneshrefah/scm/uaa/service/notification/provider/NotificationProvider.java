package ir.daneshrefah.scm.uaa.service.notification.provider;

import ir.daneshrefah.scm.uaa.domain.notification.Notification;
import ir.daneshrefah.scm.uaa.domain.notification.NotificationMedia;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-30
 */
public interface NotificationProvider {

    public void send(Notification notification);

    public NotificationMedia getType();

}

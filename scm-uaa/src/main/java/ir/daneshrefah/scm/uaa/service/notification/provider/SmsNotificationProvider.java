package ir.daneshrefah.scm.uaa.service.notification.provider;

import ir.daneshrefah.scm.uaa.domain.notification.Notification;
import ir.daneshrefah.scm.uaa.domain.notification.NotificationType;
import org.springframework.stereotype.Component;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-30
 */
@Component
public class SmsNotificationProvider implements NotificationProvider {

    @Override
    public void send(Notification notification) {

    }

    @Override
    public NotificationType getType() {
        return NotificationType.SMS;
    }

}

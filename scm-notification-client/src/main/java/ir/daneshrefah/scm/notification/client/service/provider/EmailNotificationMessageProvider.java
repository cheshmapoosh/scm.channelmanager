package ir.daneshrefah.scm.notification.client.service.provider;

import ir.daneshrefah.scm.common.model.notification.NotificationMessage;
import ir.daneshrefah.scm.common.model.notification.constants.NotificationMedia;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-18
 */
@Component
public class EmailNotificationMessageProvider implements NotificationMessageProvider {

    @Override
    public void send(NotificationMessage notificationMessage) {

    }

    @Override
    public boolean supports(NotificationMessage notificationMessage) {
        return Objects.nonNull(notificationMessage) && Objects.nonNull(notificationMessage.getRequest()) &&
                NotificationMedia.EMAIL.equals(notificationMessage.getRequest().getMedia());
    }

}

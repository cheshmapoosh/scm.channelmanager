package ir.daneshrefah.scm.notification.client.service.provider;


import ir.daneshrefah.scm.common.model.notification.Notification;
import ir.daneshrefah.scm.common.model.notification.NotificationMedia;
import ir.daneshrefah.scm.common.model.notification.NotificationQueueModel;
import ir.daneshrefah.scm.common.model.notification.NotificationStatus;
import ir.daneshrefah.scm.notification.client.service.spec.NotificationQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-30
 */
@Component
@RequiredArgsConstructor
public class SmsNotificationProvider implements NotificationProvider {

    private final NotificationQueueService notificationQueueService;

    @Override
    public void send(Notification notification) {
            NotificationQueueModel notificationQueueModel = new NotificationQueueModel()
                    .setMedia(notification.getMedia())
                    .setMessageTemplateCode(notification.getMessageTemplate().getCode().getValue())
                    .setStatus(NotificationStatus.QUEUE)
                    .setMessage(notification.getBody())
                    .setTryCount(notification.getMessageTemplate().getTryCount())
                    .setExpiration(notification.getExpiration());
            notificationQueueService.add(notificationQueueModel);
    }

    @Override
    public NotificationMedia getType() {
        return NotificationMedia.SMS;
    }


}

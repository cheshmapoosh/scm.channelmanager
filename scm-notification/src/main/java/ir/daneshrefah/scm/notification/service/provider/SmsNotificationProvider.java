package ir.daneshrefah.scm.notification.service.provider;


import ir.daneshrefah.scm.common.model.notification.Notification;
import ir.daneshrefah.scm.common.model.notification.NotificationMedia;
import ir.daneshrefah.scm.common.model.notification.NotificationQueueModel;
import ir.daneshrefah.scm.common.model.notification.NotificationStatus;
import ir.daneshrefah.scm.notification.client.spec.NotificationQueueService;
import ir.daneshrefah.scm.notification.config.NotificationConfigProperties;
import ir.daneshrefah.scm.notification.config.SMSConfig;
import ir.daneshrefah.scm.notification.repository.NotificationLogRepository;
import ir.daneshrefah.scm.notification.service.NotificationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

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
    private final NotificationConfigProperties configProperties;
    private final NotificationLogService notificationLogService;

    @Override
    public void send(Notification notification) {
        try {
            NotificationQueueModel notificationQueueModel = new NotificationQueueModel()
                    .setMedia(notification.getMedia())
                    .setMessageTemplate(notification.getMessageTemplate())
                    .setStatus(NotificationStatus.SENDING)
                    .setMessage(notification.getBody())
                    .setExpiration(getNotificationExpiration(notification));
            notificationQueueService.add(notificationQueueModel);
            notificationLogService.logNotificationEvent(notification, NotificationStatus.SENDING);
        } catch (Exception e) {
            notificationLogService.logNotificationEvent(notification, NotificationStatus.FAILED, e);
        }
    }

    @Override
    public NotificationMedia getType() {
        return NotificationMedia.SMS;
    }

    private LocalDateTime getNotificationExpiration(Notification notification) {
        SMSConfig smsConfig = configProperties.getSmsConfig();
        if (Objects.nonNull(smsConfig)) {
            int maxSecondsRetryExpiration = smsConfig.getMaxSecondsRetryExpiration();
            if (maxSecondsRetryExpiration != 0) {
                return LocalDateTime.now().plusSeconds(maxSecondsRetryExpiration);
            }
        }
        return notification.getExpiration();
    }

}

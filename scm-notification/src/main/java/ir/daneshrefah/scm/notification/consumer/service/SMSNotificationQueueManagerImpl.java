package ir.daneshrefah.scm.notification.consumer.service;

import ir.daneshrefah.scm.cache.client.distribution.spec.DistributedLock;
import ir.daneshrefah.scm.common.model.notification.Notification;
import ir.daneshrefah.scm.common.model.notification.constants.NotificationMedia;
import ir.daneshrefah.scm.notification.consumer.config.prop.ConfigProperties;
import ir.daneshrefah.scm.notification.consumer.log.NotificationLogProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class SMSNotificationQueueManagerImpl extends NotificationQueueManager {


    public SMSNotificationQueueManagerImpl(NotificationQueueService notificationQueueService,
                                           NotificationLogProvider logProvider,
                                           DistributedLock<List<Notification>> distributedLock,
                                           ConfigProperties configProperties) {
        super(notificationQueueService, logProvider,distributedLock,configProperties);
    }

    @Override
    public void execution(Notification notification) {
        //TODO adding to SMS MQ
    }

    @Override
    public NotificationMedia supportedMedia() {
        return NotificationMedia.SMS;
    }


}

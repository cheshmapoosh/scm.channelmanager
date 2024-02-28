package ir.daneshrefah.scm.notification.service;

import ir.daneshrefah.scm.cache.client.distribution.spec.DistributedLock;
import ir.daneshrefah.scm.common.model.notification.NotificationMedia;
import ir.daneshrefah.scm.common.model.notification.NotificationQueueModel;
import ir.daneshrefah.scm.notification.log.NotificationLogProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SMSNotificationQueueManagerImpl extends NotificationQueueManager {


    public SMSNotificationQueueManagerImpl(NotificationQueueService notificationQueueService,
                                           NotificationLogProvider logProvider,
                                           DistributedLock<Object> distributedLock) {
        super(notificationQueueService, logProvider,distributedLock);
    }

    @Override
    public void execution(NotificationQueueModel notificationQueueModel) {
        //TODO adding to SMS MQ
        log.info(" >>> send to sms queue");
    }

    @Override
    public NotificationMedia supportedMedia() {
        return NotificationMedia.SMS;
    }


}

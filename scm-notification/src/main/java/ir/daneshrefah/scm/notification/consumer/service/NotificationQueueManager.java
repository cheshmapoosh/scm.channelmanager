package ir.daneshrefah.scm.notification.consumer.service;

import ir.daneshrefah.scm.cache.client.distribution.spec.DistributedJob;
import ir.daneshrefah.scm.cache.client.distribution.spec.DistributedLock;
import ir.daneshrefah.scm.common.model.notification.MessageTemplate;
import ir.daneshrefah.scm.common.model.notification.Notification;
import ir.daneshrefah.scm.common.model.notification.NotificationLog;
import ir.daneshrefah.scm.common.model.notification.constants.NotificationMedia;
import ir.daneshrefah.scm.common.model.notification.constants.NotificationStatus;
import ir.daneshrefah.scm.notification.consumer.config.prop.ConfigProperties;
import ir.daneshrefah.scm.notification.consumer.config.prop.Consumer;
import ir.daneshrefah.scm.notification.consumer.log.NotificationLogProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static ir.daneshrefah.scm.common.model.notification.constants.NotificationStatus.*;

@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(value = "scm.cache.client.config.distributed", havingValue = "true")
public abstract class NotificationQueueManager {

    private static final String LOCK_GROUP = "notification-queue";
    private static final AtomicBoolean PROCESSING = new AtomicBoolean(false);
    private final NotificationQueueService notificationQueueService;
    private final NotificationLogProvider logProvider;
    private final DistributedLock<List<Notification>> distributedLock;
    private final ConfigProperties configProperties;

    public abstract void execution(Notification notification);

    public abstract NotificationMedia supportedMedia();


    @Scheduled(fixedRate = 60_000)
    public void jobSchedule() {
        if (!PROCESSING.get()) {
            Thread worker = new Thread(() -> {
                PROCESSING.set(true);
                while (notificationQueueService.findAllAvailableCount() > 0) {
                    internalJobExecution();
                }
                PROCESSING.set(false);
            });
            worker.start();
        }
    }


    private void internalJobExecution() {
        findAllPagedSynchronized()
                .stream().filter(notification -> notification.getMedia().equals(supportedMedia()))
                .forEach(notification -> {
                    if (hasExpiration(notification)) {
                        try {
                            notification.setTryCount(notification.getTryCount() - 1);
                            execution(notification);
                            logNotificationEvent(notification, null, SENT);
                            removeQueue(notification);
                        } catch (Exception e) {
                            notification.setStatus(RE_TRYING);
                            notificationQueueService.updateStatusAndTryCountAndLastModified(notification);
                            logNotificationEvent(notification, e, RE_TRYING);
                        }
                    } else {
                        logNotificationEvent(notification, null, FAILED);
                        removeQueue(notification);
                    }
                });
    }


    private List<Notification> findAllPagedSynchronized() {
        return distributedLock.synchronizedException(LOCK_GROUP, new DistributedJob<>() {
            @Override
            public List<Notification> accepted() {
                List<Notification> allAvailable = notificationQueueService.findAllAvailable(500);
                allAvailable.addAll(findAllExpiredSending());
                allAvailable.forEach(notification -> {
                    notification.setStatus(SENDING);
                    notificationQueueService.updateStatusAndTryCountAndLastModified(notification);
                    logNotificationEvent(notification,null, SENDING);
                });
                return allAvailable;
            }

            @Override
            public List<Notification> rejected() {
                return new ArrayList<>();
            }
        });
    }

    private void removeQueue(Notification notification) {
        notificationQueueService.remove(notification);
    }

    private void logNotificationEvent(Notification notification, Exception e, NotificationStatus status) {
        NotificationLog notificationLog = mapToNotificationLog(notification, e, status);
        notificationLog.setBody(notification.getBody());
        logProvider.log(notificationLog);
    }

    private boolean hasExpiration(Notification notification) {
        Integer tryCount = notification.getTryCount();
        LocalDateTime expiration = notification.getExpiration();
        return tryCount != 0 && LocalDateTime.now().isBefore(expiration);
    }

    private NotificationLog mapToNotificationLog(Notification notification, Exception exception, NotificationStatus status) {
        NotificationLog notificationLog = new NotificationLog();
        MessageTemplate messageTemplate = notification.getMessageTemplate();
        notificationLog.setMessageTemplate(messageTemplate);
        notificationLog.setMedia(notification.getMedia());
        notificationLog.setRecipient(notification.getRecipient());
        notificationLog.setBody(notification.getBody());
        notificationLog.setError(Objects.nonNull(exception) ? exception.getMessage() : null);
        notificationLog.setCreateDate(LocalDateTime.now());
        notificationLog.setCreator(notificationLog.getCreator());
        notificationLog.setStatus(status);
        return notificationLog;
    }

    private List<Notification> findAllExpiredSending(){
        int maxSendingStatusMinute = 0;
        Consumer consumer = configProperties.getConsumer();
        if (Objects.nonNull(consumer) && Objects.nonNull(consumer.getDatabaseQueueConfig()) ){
            maxSendingStatusMinute = consumer.getDatabaseQueueConfig().getMaxSendingStatusMinute();
        }
        return notificationQueueService.findAllFreezeSending(500, Duration.ofMinutes(maxSendingStatusMinute));
    }


}

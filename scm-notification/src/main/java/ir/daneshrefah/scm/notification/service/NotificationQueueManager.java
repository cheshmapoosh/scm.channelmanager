package ir.daneshrefah.scm.notification.service;

import ir.daneshrefah.scm.cache.client.distribution.spec.DistributedJob;
import ir.daneshrefah.scm.cache.client.distribution.spec.DistributedLock;
import ir.daneshrefah.scm.common.model.notification.*;
import ir.daneshrefah.scm.notification.log.NotificationLogProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static ir.daneshrefah.scm.common.model.notification.NotificationStatus.*;
import static ir.daneshrefah.scm.notification.config.ConfigConstants.SCM_CACHE_DISTRIBUTION_STATUS;

@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(value = SCM_CACHE_DISTRIBUTION_STATUS,havingValue = "true")
public abstract class NotificationQueueManager {

    private static final String LOCK_GROUP = "notification-queue";
    private static final AtomicBoolean PROCESSING = new AtomicBoolean(false);
    private final NotificationQueueService notificationQueueService;
    private final NotificationLogProvider logProvider;
    private final DistributedLock<Object> distributedLock;

    public abstract void execution(NotificationQueueModel notificationQueueModel);

    public abstract NotificationMedia supportedMedia();


    @Scheduled(fixedRate = 60_000)
    public void jobSchedule() {
        if (!PROCESSING.get()) {
            Thread worker = new Thread(() -> {
                PROCESSING.set(true);
                    distributedLock.syncroziedExceution(LOCK_GROUP,new DistributedJob<>() {
                        @Override
                        public Object accepted() {
                            while (hasUnSendMessage()) {
                                internalJobExecution();
                            }
                            return null;
                        }

                        @Override
                        public Object rejected() {
                            return null;
                        }
                    });
                PROCESSING.set(false);
            });
            worker.start();
        }
    }

    private boolean hasUnSendMessage() {
        return notificationQueueService.findAllAvailableCount() > 0;
    }


    private void internalJobExecution() {
        notificationQueueService
                .findAllAvailable(500)
                .stream().filter(notificationQueueModel -> notificationQueueModel.getMedia().equals(supportedMedia()))
                .forEach(notification -> {
                    if (hasExpiration(notification)) {
                        try {
                            notification.setTryCount(notification.getTryCount() - 1);
                            execution(notification);
                            logNotificationEvent(notification,null,SENT);
                            removeQueue(notification);
                        } catch (Exception e) {
                            updateQueueState(notification, RE_TRYING,e);
                            logNotificationEvent(notification,e,RE_TRYING);
                        }
                    } else {
                        logNotificationEvent(notification,null,FAILED);
                        removeQueue(notification);
                    }
                });
    }

    private void updateQueueState(NotificationQueueModel queueModel, NotificationStatus status,Exception e) {
        queueModel.setStatus(status);
        notificationQueueService.updateStatusAndTryCountAndLastModified(queueModel);
    }

    private void removeQueue(NotificationQueueModel queueModel) {
        notificationQueueService.remove(queueModel);
    }

    private void logNotificationEvent(NotificationQueueModel queueModel, Exception e, NotificationStatus status) {
        NotificationLog notificationLog = mapToNotificationLog(queueModel,e,status);
        notificationLog.setBody(queueModel.getMessage());
        logProvider.log(notificationLog);
    }

    private boolean hasExpiration(NotificationQueueModel queueModel) {
        Integer tryCount = queueModel.getTryCount();
        LocalDateTime expiration = queueModel.getExpiration();
        return tryCount != 0 && LocalDateTime.now().isBefore(expiration);
    }

    private NotificationLog mapToNotificationLog(NotificationQueueModel notification, Exception exception, NotificationStatus status){
        NotificationLog notificationLog = new NotificationLog();
        MessageTemplate messageTemplate = new MessageTemplate();
        messageTemplate.setId(notification.getMessageTemplateId());
        notificationLog.setMessageTemplate(messageTemplate);
        notificationLog.setMedia(notification.getMedia());
        notificationLog.setRecipient(notification.getRecipient());
        notificationLog.setBody(notification.getMessage());
        notificationLog.setError(Objects.nonNull(exception) ? exception.getMessage() : null);
        notificationLog.setCreateDate(LocalDateTime.now());
        notificationLog.setCreator(notificationLog.getCreator());
        notificationLog.setStatus(status);
        return notificationLog;
    }


}

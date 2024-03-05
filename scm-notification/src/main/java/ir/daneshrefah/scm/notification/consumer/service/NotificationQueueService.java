package ir.daneshrefah.scm.notification.consumer.service;

import ir.daneshrefah.scm.common.data.entity.notification.NotificationEntity;
import ir.daneshrefah.scm.common.data.mapper.notification.NotificationMapper;
import ir.daneshrefah.scm.common.data.repository.notification.NotificationRepository;
import ir.daneshrefah.scm.common.model.notification.Notification;
import ir.daneshrefah.scm.utils.date.DateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationQueueService {
    private final NotificationRepository notificationQueueRepository;

    public List<Notification> findAllAvailable(int size) {
        return NotificationMapper
                .INSTANCE
                .toModel(notificationQueueRepository.findAllAvailable(PageRequest.ofSize(size).withSort(Sort.by("id").descending())));

    }

    public List<Notification> findAllFreezeSending(int size, Duration maxSendingTimeWait) {
        return NotificationMapper
                .INSTANCE
                .toModel(notificationQueueRepository
                        .findAllSending(
                                PageRequest.ofSize(size).withSort(Sort.by("id").descending()),
                                DateUtils.LocalDateTimeTools.plus(DateUtils.LocalDateTimeTools.current(), maxSendingTimeWait))
                );
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW,transactionManager = "notificationTransactionManager")
    public void updateStatusAndTryCountAndLastModified(Notification notification) {
        notificationQueueRepository.findById(notification.getId())
                .ifPresent(foundNotification -> {
                    customUpdate(foundNotification, notification);
                    notificationQueueRepository.save(foundNotification);
                });
    }

    private void customUpdate(NotificationEntity found, Notification src) {
        found.setTryCount(src.getTryCount());
        found.setLastEditDate(LocalDateTime.now());
        found.setStatus(src.getStatus());
    }

    @Transactional(transactionManager = "notificationTransactionManager")
    public void remove(Notification queueModel) {
        notificationQueueRepository.delete(NotificationMapper.INSTANCE.toEntity(queueModel));
    }

    public long findAllAvailableCount() {
        return notificationQueueRepository.findAllAvailableCount();
    }
}

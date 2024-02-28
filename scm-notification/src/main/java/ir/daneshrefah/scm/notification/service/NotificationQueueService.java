package ir.daneshrefah.scm.notification.service;

import ir.daneshrefah.scm.common.data.entity.notification.NotificationQueueEntity;
import ir.daneshrefah.scm.common.data.mapper.notification.NotificationQueueEntityMapper;
import ir.daneshrefah.scm.common.data.repository.notification.NotificationQueueRepository;
import ir.daneshrefah.scm.common.model.notification.NotificationQueueModel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationQueueService {
    private final NotificationQueueRepository notificationQueueRepository;

    public List<NotificationQueueModel> findAllAvailable(int size) {
        return NotificationQueueEntityMapper
                .INSTANCE
                .toModel(notificationQueueRepository.findAllAvailable(PageRequest.ofSize(size).withSort(Sort.by("id").descending())));

    }

    public void updateStatusAndTryCountAndLastModified(NotificationQueueModel notificationQueueModel) {
        notificationQueueRepository.findById(notificationQueueModel.getId())
                .ifPresent(foundNotification -> {
                    customUpdate(foundNotification, notificationQueueModel);
                    notificationQueueRepository.save(foundNotification);
                });
    }

    private void customUpdate(NotificationQueueEntity found, NotificationQueueModel src) {
        found.setTryCount(src.getTryCount());
        found.setLastEditDate(LocalDateTime.now());
        found.setStatus(src.getStatus());
    }

    public void remove(NotificationQueueModel queueModel) {
        notificationQueueRepository.delete(NotificationQueueEntityMapper.INSTANCE.toEntity(queueModel));
    }

    public long findAllAvailableCount() {
        return notificationQueueRepository.findAllAvailableCount();
    }
}

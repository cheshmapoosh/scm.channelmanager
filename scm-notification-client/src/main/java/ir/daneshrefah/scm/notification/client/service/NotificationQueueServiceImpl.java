package ir.daneshrefah.scm.notification.client.service;

import ir.daneshrefah.scm.common.model.notification.NotificationQueueModel;
import ir.daneshrefah.scm.common.model.notification.NotificationStatus;
import ir.daneshrefah.scm.notification.client.repository.NotificationQueueRepository;
import ir.daneshrefah.scm.notification.client.repository.domain.NotificationQueueEntity;
import ir.daneshrefah.scm.notification.client.repository.mapper.NotificationQueueEntityMapper;
import ir.daneshrefah.scm.notification.client.service.log.NotificationLogService;
import ir.daneshrefah.scm.notification.client.service.spec.NotificationQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationQueueServiceImpl implements NotificationQueueService {
    private final NotificationQueueRepository notificationQueueRepository;

    @Override
    public void add(NotificationQueueModel notificationQueueModel) {
        NotificationQueueEntity entity = NotificationQueueEntityMapper.INSTANCE.toEntity(notificationQueueModel);
        notificationQueueRepository.save(entity);
    }

    @Override
    public void changeStatus(NotificationQueueModel notificationQueueModel, NotificationStatus status) {
        notificationQueueRepository
                .findById(notificationQueueModel.getId())
                .ifPresent(found -> {
                    found.setStatus(status);
                    notificationQueueRepository.save(found);
                });

    }

    @Override
    public Optional<NotificationQueueModel> findById(String id) {
        return notificationQueueRepository
                .findById(id)
                .map(NotificationQueueEntityMapper.INSTANCE::toModel);
    }
}

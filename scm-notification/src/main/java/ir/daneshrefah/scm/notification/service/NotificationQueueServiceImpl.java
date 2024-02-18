package ir.daneshrefah.scm.notification.service;

import ir.daneshrefah.scm.common.model.notification.NotificationQueueModel;
import ir.daneshrefah.scm.common.model.notification.NotificationStatus;
import ir.daneshrefah.scm.notification.client.spec.NotificationQueueService;
import ir.daneshrefah.scm.notification.domain.NotificationQueueEntity;
import ir.daneshrefah.scm.notification.repository.NotificationQueueRepository;
import ir.daneshrefah.scm.notification.repository.mapper.NotificationQueueEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationQueueServiceImpl implements NotificationQueueService {
    private final NotificationQueueRepository notificationQueueRepository;

    @Override
    public void add(NotificationQueueModel notificationQueueModel) {
        notificationQueueRepository.save(NotificationQueueEntityMapper.INSTANCE.toEntity(notificationQueueModel));
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

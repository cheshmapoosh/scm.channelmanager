package ir.daneshrefah.scm.notification.client.spec;

import ir.daneshrefah.scm.common.model.notification.NotificationQueueModel;
import ir.daneshrefah.scm.common.model.notification.NotificationStatus;

import java.util.Optional;

public interface NotificationQueueService {
    void add(NotificationQueueModel notificationQueueModel);
    void changeStatus(NotificationQueueModel notificationQueueModel, NotificationStatus status);
    Optional<NotificationQueueModel> findById(String id);
}

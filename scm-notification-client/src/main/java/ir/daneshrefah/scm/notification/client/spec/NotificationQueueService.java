package ir.daneshrefah.scm.notification.client.spec;

import ir.daneshrefah.scm.common.model.notification.NotificationQueueModel;
import ir.daneshrefah.scm.common.model.notification.NotificationStatus;

public interface NotificationQueueService {
    void add(NotificationQueueModel notificationQueueModel);
    void changeStatus(NotificationQueueModel notificationQueueModel, NotificationStatus status);
}

package ir.daneshrefah.scm.notification.client.service.spec;


import ir.daneshrefah.scm.common.model.notification.NotificationRequest;

public interface NotificationService {

    void sendNotification(NotificationRequest notificationRequest);

}

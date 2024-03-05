package ir.daneshrefah.scm.notification.consumer.log;


import ir.daneshrefah.scm.common.model.notification.NotificationLog;

public interface NotificationLogProvider {
    void log(NotificationLog notificationLog);
}

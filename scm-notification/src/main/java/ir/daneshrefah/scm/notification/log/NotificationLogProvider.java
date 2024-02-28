package ir.daneshrefah.scm.notification.log;


import ir.daneshrefah.scm.common.model.notification.NotificationLog;

public interface NotificationLogProvider {
    void log(NotificationLog notificationLog);
}

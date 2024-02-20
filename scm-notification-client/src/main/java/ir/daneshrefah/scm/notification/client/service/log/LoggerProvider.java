package ir.daneshrefah.scm.notification.client.service.log;

import ir.daneshrefah.scm.notification.client.repository.domain.NotificationLog;

public interface LoggerProvider {
    void log(NotificationLog notificationLog);
}

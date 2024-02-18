package ir.daneshrefah.scm.notification.service;

import ir.daneshrefah.scm.common.model.notification.NotificationRequest;
import ir.daneshrefah.scm.notification.client.spec.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import static ir.daneshrefah.scm.notification.config.ConfigProperties.SCM_NOTIFICATION_STATUS;

@Service
@ConditionalOnProperty(value = SCM_NOTIFICATION_STATUS,havingValue = "false")
@Slf4j
public class DisabledNotificationServiceImpl implements NotificationService {
    @Override
    public void sendNotification(NotificationRequest request) {
        log.warn(">>> notification couldn't send (set enabled = true )");
    }
}

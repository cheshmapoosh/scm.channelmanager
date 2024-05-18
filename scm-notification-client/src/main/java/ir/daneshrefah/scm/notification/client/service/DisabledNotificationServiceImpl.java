package ir.daneshrefah.scm.notification.client.service;

import ir.daneshrefah.scm.common.model.notification.NotificationRequest;
import ir.daneshrefah.scm.notification.client.service.spec.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
public class DisabledNotificationServiceImpl implements NotificationService {

    @Override
    public void sendNotification(NotificationRequest request) {
        log.error(">>> notification couldn't send (set enabled = true )");
    }
}

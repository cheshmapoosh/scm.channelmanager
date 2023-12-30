package ir.daneshrefah.scm.uaa.service.notification;

import ir.daneshrefah.scm.uaa.domain.notification.Notification;
import ir.daneshrefah.scm.uaa.domain.notification.NotificationType;
import ir.daneshrefah.scm.uaa.service.notification.provider.NotificationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-30
 */
@Service
public class NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationService.class);

    private final Map<NotificationType, NotificationProvider> providers;

    public NotificationService(List<NotificationProvider> providers) {
        this.providers = providers.stream()
                .collect(Collectors.toMap(NotificationProvider::getType, Function.identity()));
    }

    public void sendNotification(Notification notification) {
        NotificationProvider provider = providers.get(notification.getType());
        if (null == provider) {
            LOGGER.error("unsupported notification type: " + notification.getType());
            throw new RuntimeException("Unsupported notification type: " + notification.getType());
            //TODO create specific exception class
        }

        provider.send(notification);
    }

}

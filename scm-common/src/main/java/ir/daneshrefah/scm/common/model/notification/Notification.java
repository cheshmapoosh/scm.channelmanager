package ir.daneshrefah.scm.common.model.notification;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-05
 */
@Getter
@Builder
public class Notification {

    private final NotificationMedia media;
    private final String recipient;
    private final MessageTemplate messageTemplate;
    private final String body;
    private final LocalDateTime expiration;

}

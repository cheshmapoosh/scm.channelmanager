package ir.daneshrefah.scm.uaa.domain.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-30
 */
@Getter
@AllArgsConstructor
public abstract class Notification {

    private NotificationType type;
    private String message;
    private String recipient;
    private MessageType messageType;

}

package ir.daneshrefah.scm.common.model.notification;

import ir.daneshrefah.scm.common.model.notification.constants.NotificationMedia;
import ir.daneshrefah.scm.common.model.notification.constants.NotificationStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-05
 */
@Getter
@Setter
@Accessors(chain = true)
public class NotificationLog  {
    private String terminalCode;
    private String username;
    private String recipient;
    private NotificationMedia media;
    private NotificationData data;
    private NotificationStatus status;
    private MessageTemplate messageTemplate;
    private String body;
    private String error;
    private String creator;
    private LocalDateTime createDate;
}

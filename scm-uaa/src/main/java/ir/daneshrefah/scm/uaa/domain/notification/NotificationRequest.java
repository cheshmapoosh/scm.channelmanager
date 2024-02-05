package ir.daneshrefah.scm.uaa.domain.notification;

import lombok.Builder;
import lombok.Getter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-30
 */
@Getter
@Builder
public class NotificationRequest {

    private NotificationMedia media;
    private String recipient;
    private NotificationData data;
    private String messageTemplateCode;

}

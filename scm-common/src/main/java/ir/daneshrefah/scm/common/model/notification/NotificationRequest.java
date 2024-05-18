package ir.daneshrefah.scm.common.model.notification;

import ir.daneshrefah.scm.common.model.message.IssuerInfo;
import ir.daneshrefah.scm.common.model.notification.constants.NotificationMedia;
import ir.daneshrefah.scm.common.model.notification.constants.NotificationTemplate;
import ir.daneshrefah.scm.common.model.person.PersonType;
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

    private NotificationTemplate template;
    private NotificationMedia media;
    private String recipient;
    private PersonType recipientType;
    private String recipientUsername;
    private NotificationData data;
    private String terminalCode;
    private IssuerInfo issuerInfo;

}

package ir.daneshrefah.scm.common.model.notification;

import ir.daneshrefah.scm.common.model.notification.constants.TemplateCode;
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
    private String terminalCode;
    private TemplateCode templateCode;
    private String username;
    private String createdBy;

}

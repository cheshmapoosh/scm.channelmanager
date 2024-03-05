package ir.daneshrefah.scm.common.model.notification;

import ir.daneshrefah.scm.common.BaseModel;
import ir.daneshrefah.scm.common.model.notification.constants.NotificationMedia;
import ir.daneshrefah.scm.common.model.notification.constants.NotificationStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
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
public class Notification extends BaseModel<String> implements Serializable {

    private  NotificationMedia media;
    private  String recipient;
    private  MessageTemplate messageTemplate;
    private  String body;
    private  LocalDateTime expiration;
    private  NotificationStatus status;
    private  Integer tryCount;

}

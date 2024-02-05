package ir.daneshrefah.scm.uaa.repository.authentication.notification;

import ir.daneshrefah.scm.common.data.entity.AbstractEntity;
import ir.daneshrefah.scm.uaa.domain.notification.MessageTemplate;
import ir.daneshrefah.scm.uaa.domain.notification.NotificationData;
import ir.daneshrefah.scm.uaa.domain.notification.NotificationMedia;
import ir.daneshrefah.scm.uaa.domain.notification.NotificationRequest;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-05
 */
@Getter
@Setter
@Entity
@Table(name = "TBL_SNT_NOTIFICATION_LOG")
public class NotificationLogEntity extends AbstractEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NOTIFICATION_LOG_ID")
    private Long id;
    private String terminalCode;
    private String username;
    private String recipient;
    private NotificationMedia media;
//    private NotificationData data;
    private MessageTemplate messageTemplate;
    private String body;
    private Integer status;
    private String error;

}

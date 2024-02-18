package ir.daneshrefah.scm.notification.domain;

import ir.daneshrefah.scm.common.data.entity.AbstractEntity;

import ir.daneshrefah.scm.common.model.notification.NotificationData;
import ir.daneshrefah.scm.common.model.notification.NotificationMedia;
import ir.daneshrefah.scm.common.model.notification.NotificationStatus;
import ir.daneshrefah.scm.notification.repository.converters.NotificationDataConverter;
import ir.daneshrefah.scm.notification.repository.converters.NotificationStatusConverter;
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
    @Column(name = "TERMINAL_CODE")
    private String terminalCode;
    @Column(name = "USERNAME")
    private String username;
    @Column(name = "RECIPIENT")
    private String recipient;
    @Column(name = "MEDIA")
    @Enumerated(EnumType.STRING)
    private NotificationMedia media;
    @Convert(converter = NotificationDataConverter.class)
    private NotificationData data;
    @OneToOne(cascade = CascadeType.REFRESH)
    @JoinColumn(name = "MESSAGE_TEMPLATE_ID")
    private MessageTemplateEntity messageTemplate;
    @Column(name = "BODY")
    private String body;
    @Column(name = "STATUS")
    @Convert(converter = NotificationStatusConverter.class)
    private NotificationStatus status;
    @Column(name = "ERROR")
    private String error;

}

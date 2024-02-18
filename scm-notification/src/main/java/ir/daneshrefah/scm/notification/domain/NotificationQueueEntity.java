package ir.daneshrefah.scm.notification.domain;

import ir.daneshrefah.scm.common.data.entity.AbstractEntity;
import ir.daneshrefah.scm.common.model.notification.NotificationMedia;
import ir.daneshrefah.scm.common.model.notification.NotificationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "SCM_NTF_NOTIFICATION_QUEUE")
@Setter
@Getter
public class NotificationQueueEntity extends AbstractEntity<String> {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "NOTIFICATION_QUEUE_ID")
    private String id;
    @Column(name = "MEDIA")
    @Enumerated(EnumType.STRING)
    private NotificationMedia media;
    @OneToOne
    @JoinColumn(name = "MESSAGE_TEMPLATE_ID")
    private MessageTemplateEntity messageTemplate;
    @Column(name = "MESSAGE")
    private String message;
    @Column(name = "STATUS")
    private NotificationStatus status;
    @Column(name = "EXPIRATION")
    private LocalDateTime expiration;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

}

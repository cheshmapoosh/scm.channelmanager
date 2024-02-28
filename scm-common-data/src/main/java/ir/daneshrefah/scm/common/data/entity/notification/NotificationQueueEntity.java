package ir.daneshrefah.scm.common.data.entity.notification;

import ir.daneshrefah.scm.common.data.entity.AbstractDefaultEntity;
import ir.daneshrefah.scm.common.model.notification.NotificationMedia;
import ir.daneshrefah.scm.common.model.notification.NotificationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "TBL_SNT_NOTIFICATION_QUEUE")
@Setter
@Getter
public class NotificationQueueEntity extends AbstractDefaultEntity<String> {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "NOTIFICATION_QUEUE_ID")
    private String id;
    @Column(name = "MEDIA")
    @Enumerated(EnumType.STRING)
    private NotificationMedia media;
    @Column(name = "MESSAGE_TEMPLATE_ID")
    private Long messageTemplateId;
    @Column(name = "MESSAGE")
    private String message;
    @Column(name = "RECIPIENT")
    private String recipient;
    @Column(name = "STATUS")
    @Enumerated(EnumType.STRING)
    private NotificationStatus status;
    @Column(name = "EXPIRATION")
    private LocalDateTime expiration;
    @Column(name = "TRY_COUNT")
    private Integer tryCount;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

}

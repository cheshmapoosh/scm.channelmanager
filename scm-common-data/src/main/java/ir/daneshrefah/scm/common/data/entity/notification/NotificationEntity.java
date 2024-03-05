package ir.daneshrefah.scm.common.data.entity.notification;

import ir.daneshrefah.scm.common.data.entity.AbstractDefaultEntity;
import ir.daneshrefah.scm.common.model.notification.constants.NotificationMedia;
import ir.daneshrefah.scm.common.model.notification.constants.NotificationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "TBL_SNT_NOTIFICATION")
@Setter
@Getter
public class NotificationEntity extends AbstractDefaultEntity<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "NOTIFICATION_ID")
    private String id;
    @Column(name = "MEDIA")
    @Enumerated(EnumType.STRING)
    private NotificationMedia media;
    @JoinColumn(name = "MESSAGE_TEMPLATE_ID")
    @ManyToOne
    private MessageTemplateEntity messageTemplateEntity;
    @Column(name = "BODY")
    private String body;
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

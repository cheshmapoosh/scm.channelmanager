package ir.daneshrefah.scm.common.model.notification;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;


@Setter
@Getter
@Accessors(chain = true)
public class NotificationQueueModel implements Serializable {
    @JsonIgnore
    private String id;
    private NotificationMedia media;
    private MessageTemplate messageTemplate;
    private String message;
    private NotificationStatus status;
    private LocalDateTime expiration;
}
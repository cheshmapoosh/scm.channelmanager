package ir.daneshrefah.scm.common.model.notification;

import ir.daneshrefah.scm.common.BaseModel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;


@Setter
@Getter
@Accessors(chain = true)
public class NotificationQueueModel extends BaseModel<String> implements Serializable {
    private NotificationMedia media;
    private String messageTemplateCode;
    private String message;
    private NotificationStatus status;
    private Integer tryCount;
    private LocalDateTime expiration;
}
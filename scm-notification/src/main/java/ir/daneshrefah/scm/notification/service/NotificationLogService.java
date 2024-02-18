package ir.daneshrefah.scm.notification.service;

import ir.daneshrefah.scm.common.model.notification.*;
import ir.daneshrefah.scm.notification.domain.NotificationLogEntity;
import ir.daneshrefah.scm.notification.repository.NotificationLogRepository;
import ir.daneshrefah.scm.notification.repository.mapper.MessageTemplateEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class NotificationLogService {

    private final NotificationLogRepository notificationLogRepository;

    public void logNotificationEvent(Notification notification, NotificationStatus status ,Exception exception) {
        NotificationLogEntity logEntity = new NotificationLogEntity();
        NotificationRequest request = notification.getRequest();
        NotificationData data = request.getData();
        logEntity.setData(data);
        logEntity.setMessageTemplate(MessageTemplateEntityMapper.INSTANCE.toEntity(notification.getMessageTemplate()));
        logEntity.setMedia(request.getMedia());
        logEntity.setRecipient(request.getRecipient());
        logEntity.setTerminalCode((String) data.get(DataKey.TERMINAL_CODE));
        logEntity.setBody(notification.getBody());
        logEntity.setStatus(Objects.isNull(exception) ? status : NotificationStatus.FAILED);
        logEntity.setError(Objects.nonNull(exception) ? exception.getMessage() : null);
        notificationLogRepository.save(logEntity);
    }

    public void logNotificationEvent(Notification notification, NotificationStatus status ) {
        NotificationLogEntity logEntity = new NotificationLogEntity();
        NotificationRequest request = notification.getRequest();
        NotificationData data = request.getData();
        logEntity.setData(data);
        logEntity.setMessageTemplate(MessageTemplateEntityMapper.INSTANCE.toEntity(notification.getMessageTemplate()));
        logEntity.setMedia(request.getMedia());
        logEntity.setRecipient(request.getRecipient());
        logEntity.setTerminalCode((String) data.get(DataKey.TERMINAL_CODE));
        logEntity.setBody(notification.getBody());
        logEntity.setStatus(status);
        notificationLogRepository.save(logEntity);
    }

}

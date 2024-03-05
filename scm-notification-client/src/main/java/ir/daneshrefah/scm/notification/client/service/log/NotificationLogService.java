package ir.daneshrefah.scm.notification.client.service.log;

import ir.daneshrefah.scm.common.model.notification.Notification;
import ir.daneshrefah.scm.common.model.notification.NotificationData;
import ir.daneshrefah.scm.common.model.notification.NotificationRequest;
import ir.daneshrefah.scm.common.model.notification.constants.NotificationStatus;
import ir.daneshrefah.scm.common.model.notification.NotificationLog;
import ir.daneshrefah.scm.notification.client.service.MessageTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class NotificationLogService {

    private final LoggerProvider loggerProvider;
    private final MessageTemplateService messageTemplateService;

    public void logNotificationEvent(Notification notification, NotificationRequest request, NotificationStatus status,Exception exception) {
        NotificationLog notificationLog = mapToNotificationLog(request,exception,status);
        notificationLog.setBody(notification.getBody());
        loggerProvider.log(notificationLog);
    }

    public void logNotificationEvent(NotificationRequest request ,NotificationStatus status,Exception exception) {
        NotificationLog notificationLog = mapToNotificationLog(request, exception,status);
        loggerProvider.log(notificationLog);
    }

    public void logNotificationEvent(Notification notification,NotificationRequest request ,NotificationStatus status) {
      logNotificationEvent(notification,request,status,null);
    }

    private NotificationLog mapToNotificationLog(NotificationRequest request,Exception exception,NotificationStatus status){
        NotificationLog notificationLog = new NotificationLog();
        NotificationData data = request.getData();
        notificationLog.setData(data);
        notificationLog.setMessageTemplate(messageTemplateService.findMessageTemplateByCode(request.getTemplateCode()));
        notificationLog.setMedia(request.getMedia());
        notificationLog.setRecipient(request.getRecipient());
        notificationLog.setTerminalCode(request.getTerminalCode());
        notificationLog.setBody("null");
        notificationLog.setError(Objects.nonNull(exception) ? exception.getMessage() : null);
        notificationLog.setCreateDate(LocalDateTime.now());
        notificationLog.setCreator(request.getCreatedBy());
        notificationLog.setStatus(status);
        return notificationLog;
    }


}

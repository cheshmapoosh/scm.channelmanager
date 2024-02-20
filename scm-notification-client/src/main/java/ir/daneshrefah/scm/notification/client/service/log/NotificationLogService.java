package ir.daneshrefah.scm.notification.client.service.log;

import ir.daneshrefah.scm.common.model.notification.Notification;
import ir.daneshrefah.scm.common.model.notification.NotificationData;
import ir.daneshrefah.scm.common.model.notification.NotificationRequest;
import ir.daneshrefah.scm.notification.client.repository.domain.NotificationLog;
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

    public void logNotificationEvent(Notification notification,Exception exception) {
        NotificationLog notificationLog = craeteNotificationLog(notification.getRequest(),exception);
        notificationLog.setBody(notification.getBody());
        loggerProvider.log(notificationLog);
    }

    public void logNotificationEvent(NotificationRequest request ,Exception exception) {
        NotificationLog notificationLog = craeteNotificationLog(request, exception);
        loggerProvider.log(notificationLog);
    }

    public void logNotificationEvent(Notification notification ) {
      logNotificationEvent(notification,null);
    }

    private NotificationLog craeteNotificationLog(NotificationRequest request,Exception exception){
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
        return notificationLog;
    }


}

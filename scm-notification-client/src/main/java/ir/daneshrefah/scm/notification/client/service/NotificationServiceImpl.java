package ir.daneshrefah.scm.notification.client.service;

import ir.daneshrefah.scm.common.model.notification.*;
import ir.daneshrefah.scm.notification.client.exception.NotFoundSupportedBodyProcessorException;
import ir.daneshrefah.scm.notification.client.exception.NotificationBodyProcessException;
import ir.daneshrefah.scm.notification.client.exception.NotificationBodyProcessorDoesNotExistsException;
import ir.daneshrefah.scm.notification.client.service.log.NotificationLogService;
import ir.daneshrefah.scm.notification.client.service.spec.NotificationQueueService;
import ir.daneshrefah.scm.notification.client.service.spec.NotificationService;
import ir.daneshrefah.scm.notification.client.service.template.NotificationBodyProcessor;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-30
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final List<NotificationBodyProcessor> bodyProcessors;
    private final NotificationLogService notificationLogService;
    private final MessageTemplateService messageTemplateService;
    private final NotificationQueueService notificationQueueService;

    public void sendNotification(NotificationRequest request) {
        try {
            Notification notification = createNotification(request);
            notificationLogService.logNotificationEvent(notification,request,NotificationStatus.DRAFT);
            sendNotificationInternal(notification);
            notificationLogService.logNotificationEvent(notification,request,NotificationStatus.QUEUE);
        } catch (Exception e) {
            notificationLogService.logNotificationEvent(request,NotificationStatus.FAILED,e);
        }
    }


    private void sendNotificationInternal(Notification notification) {
        NotificationQueueModel notificationQueueModel = new NotificationQueueModel()
                .setMedia(notification.getMedia())
                .setMessageTemplateId(notification.getMessageTemplate().getId())
                .setStatus(NotificationStatus.QUEUE)
                .setMessage(notification.getBody())
                .setTryCount(notification.getMessageTemplate().getTryCount())
                .setExpiration(notification.getExpiration());
        notificationQueueService.add(notificationQueueModel);
    }

    private Notification createNotification(NotificationRequest notificationRequest) {
           MessageTemplate messageTemplate = messageTemplateService.findMessageTemplateByCode(notificationRequest.getTemplateCode());
           return Notification.builder()
                   .media(notificationRequest.getMedia())
                   .recipient(notificationRequest.getRecipient())
                   .messageTemplate(messageTemplate)
                   .body(extractNotificationBody(messageTemplate, notificationRequest.getData(),notificationRequest))
                   .expiration(LocalDateTime.now().plusMinutes(Objects.isNull(messageTemplate.getMaxMinutesExpiration()) ? 0 : messageTemplate.getMaxMinutesExpiration()))
                   .build();
    }

    private String extractNotificationBody(MessageTemplate template, NotificationData data,NotificationRequest request) {
        if (Objects.isNull(bodyProcessors) || bodyProcessors.isEmpty()) {
            throw new NotificationBodyProcessorDoesNotExistsException();
        }
        for (NotificationBodyProcessor bodyProcessor : bodyProcessors) {
            try {
                String body = bodyProcessor.process(template, data,request);
                if (StringUtils.isNotEmpty(body)) {
                    return body;
                }
            } catch (Exception e) {
                throw new NotificationBodyProcessException(template.getBody());
            }
        }
        throw new NotFoundSupportedBodyProcessorException(template.getCode().getValue());
    }



}

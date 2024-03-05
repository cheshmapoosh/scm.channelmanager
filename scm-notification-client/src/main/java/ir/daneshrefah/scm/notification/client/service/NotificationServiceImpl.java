package ir.daneshrefah.scm.notification.client.service;

import ir.daneshrefah.scm.common.data.entity.notification.NotificationEntity;
import ir.daneshrefah.scm.common.data.mapper.notification.NotificationMapper;
import ir.daneshrefah.scm.common.data.repository.notification.NotificationRepository;
import ir.daneshrefah.scm.common.model.notification.MessageTemplate;
import ir.daneshrefah.scm.common.model.notification.Notification;
import ir.daneshrefah.scm.common.model.notification.NotificationData;
import ir.daneshrefah.scm.common.model.notification.NotificationRequest;
import ir.daneshrefah.scm.common.model.notification.constants.NotificationStatus;
import ir.daneshrefah.scm.notification.client.exception.NotFoundSupportedBodyProcessorException;
import ir.daneshrefah.scm.notification.client.exception.NotificationBodyProcessException;
import ir.daneshrefah.scm.notification.client.exception.NotificationBodyProcessorDoesNotExistsException;
import ir.daneshrefah.scm.notification.client.service.log.NotificationLogService;
import ir.daneshrefah.scm.notification.client.service.spec.NotificationService;
import ir.daneshrefah.scm.notification.client.service.template.NotificationBodyProcessor;
import ir.daneshrefah.scm.utils.date.DateUtils;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-30
 */
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final List<NotificationBodyProcessor> bodyProcessors;
    private final NotificationLogService notificationLogService;
    private final MessageTemplateService messageTemplateService;
    private final NotificationRepository notificationRepository;

    public void sendNotification(NotificationRequest request) {
        try {
            validateNotificationRequest(request);
            Notification notification = createNotification(request);
            notificationLogService.logNotificationEvent(notification, request, NotificationStatus.DRAFT);
            sendNotificationInternal(notification);
            notificationLogService.logNotificationEvent(notification, request, NotificationStatus.QUEUE);
        } catch (Exception e) {
            notificationLogService.logNotificationEvent(request, NotificationStatus.FAILED, e);
        }
    }

    private void validateNotificationRequest(NotificationRequest request) {
        messageTemplateService.findMessageTemplateByCode(request.getTemplateCode());
    }


    private void sendNotificationInternal(Notification notification) {
        notification.setStatus(NotificationStatus.QUEUE);
        NotificationEntity entity = NotificationMapper.INSTANCE.toEntity(notification);
        notificationRepository.save(entity);
    }

    private Notification createNotification(NotificationRequest notificationRequest) {
        MessageTemplate messageTemplate = messageTemplateService.findMessageTemplateByCode(notificationRequest.getTemplateCode());
        return new Notification()
                .setMedia(notificationRequest.getMedia())
                .setRecipient(notificationRequest.getRecipient())
                .setMessageTemplate(messageTemplate)
                .setBody(extractNotificationBody(messageTemplate, notificationRequest.getData(), notificationRequest))
                .setTryCount(messageTemplate.getTryCount())
                .setExpiration(DateUtils
                        .LocalDateTimeTools.plus(DateUtils.LocalDateTimeTools.current(),
                                Duration.ofMinutes(
                                        Objects.isNull(messageTemplate.getMaxMinutesExpiration()) ? 0 : messageTemplate.getMaxMinutesExpiration())));
    }

    private String extractNotificationBody(MessageTemplate template, NotificationData data, NotificationRequest request) {
        if (Objects.isNull(bodyProcessors) || bodyProcessors.isEmpty()) {
            throw new NotificationBodyProcessorDoesNotExistsException();
        }
        for (NotificationBodyProcessor bodyProcessor : bodyProcessors) {
            try {
                String body = bodyProcessor.process(template, data, request);
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

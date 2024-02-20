package ir.daneshrefah.scm.notification.client.service;

import ir.daneshrefah.scm.common.model.notification.*;
import ir.daneshrefah.scm.notification.client.exception.NotFoundSupportedBodyProcessorException;
import ir.daneshrefah.scm.notification.client.exception.NotificationBodyProcessException;
import ir.daneshrefah.scm.notification.client.exception.NotificationBodyProcessorDoesNotExistsException;
import ir.daneshrefah.scm.notification.client.exception.NotificationProviderNotFoundException;
import ir.daneshrefah.scm.notification.client.service.log.NotificationLogService;
import ir.daneshrefah.scm.notification.client.service.provider.NotificationProvider;
import ir.daneshrefah.scm.notification.client.service.spec.NotificationService;
import ir.daneshrefah.scm.notification.client.service.template.NotificationBodyProcessor;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-30
 */
@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final Map<NotificationMedia, NotificationProvider> providers;
    private final List<NotificationBodyProcessor> bodyProcessors;
    private final NotificationLogService notificationLogService;
    private final MessageTemplateService messageTemplateService;

    public NotificationServiceImpl(List<NotificationProvider> providers, List<NotificationBodyProcessor> bodyProcessors,
                                   NotificationLogService notificationLogService,
                                   MessageTemplateService messageTemplateService) {
        this.providers = providers.stream().collect(Collectors.toMap(NotificationProvider::getType, Function.identity()));
        this.bodyProcessors = bodyProcessors;
        this.notificationLogService = notificationLogService;
        this.messageTemplateService = messageTemplateService;
    }

    public void sendNotification(NotificationRequest request) {
        try {
            Notification notification = createNotification(request);
            notificationLogService.logNotificationEvent(notification);
            sendNotificationInternal(notification);
            notificationLogService.logNotificationEvent(notification);
        } catch (Exception e) {
            notificationLogService.logNotificationEvent(request,e);
        }
    }


    private void sendNotificationInternal(Notification notification) {
        providers
                .computeIfAbsent(notification.getMedia(), (media)->{
                    throw new NotificationProviderNotFoundException(media.name());
                })
                .send(notification);
    }

    private Notification createNotification(NotificationRequest notificationRequest) {
           MessageTemplate messageTemplate = messageTemplateService.findMessageTemplateByCode(notificationRequest.getTemplateCode());
           return Notification.builder()
                   .request(notificationRequest)
                   .media(notificationRequest.getMedia())
                   .recipient(notificationRequest.getRecipient())
                   .messageTemplate(messageTemplate)
                   .body(extractNotificationBody(messageTemplate, notificationRequest.getData()))
                   .expiration(LocalDateTime.now().plusMinutes(Objects.isNull(messageTemplate.getMaxMinutesExpiration()) ? 0 : messageTemplate.getMaxMinutesExpiration()))
                   .build();
    }

    private String extractNotificationBody(MessageTemplate template, NotificationData data) {
        if (Objects.isNull(bodyProcessors) || bodyProcessors.isEmpty()) {
            throw new NotificationBodyProcessorDoesNotExistsException();
        }
        for (NotificationBodyProcessor bodyProcessor : bodyProcessors) {
            try {
                String body = bodyProcessor.process(template, data);
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

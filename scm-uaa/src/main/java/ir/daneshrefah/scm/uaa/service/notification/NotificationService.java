package ir.daneshrefah.scm.uaa.service.notification;

import ir.daneshrefah.scm.uaa.domain.notification.*;
import ir.daneshrefah.scm.uaa.exception.NotificationProviderNotFoundException;
import ir.daneshrefah.scm.uaa.repository.authentication.notification.MessageTemplateEntity;
import ir.daneshrefah.scm.uaa.repository.authentication.notification.MessageTemplateRepository;
import ir.daneshrefah.scm.uaa.repository.authentication.notification.NotificationLogEntity;
import ir.daneshrefah.scm.uaa.repository.authentication.notification.NotificationLogRepository;
import ir.daneshrefah.scm.uaa.service.notification.provider.NotificationProvider;
import ir.daneshrefah.scm.uaa.service.notification.template.NotificationBodyProcessor;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
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
public class NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationService.class);

    private final Map<NotificationMedia, NotificationProvider> providers;
    private final List<NotificationBodyProcessor> bodyProcessors;
    private final NotificationLogRepository notificationLogRepository;
    private final MessageTemplateRepository messageTemplateRepository;
    private List<MessageTemplateEntity> messageTemplates;

    public NotificationService(List<NotificationProvider> providers, List<NotificationBodyProcessor> bodyProcessors,
                               NotificationLogRepository notificationLogRepository,
                               MessageTemplateRepository messageTemplateRepository) {
        this.providers = providers.stream()
                .collect(Collectors.toMap(NotificationProvider::getType, Function.identity()));
        this.bodyProcessors = bodyProcessors;
        this.notificationLogRepository = notificationLogRepository;
        this.messageTemplateRepository = messageTemplateRepository;
    }

    public void sendNotification(NotificationRequest request) {
        Instant startTime = Instant.now();
        Exception exception = null;
        try {
            sendNotificationInternal(request);
        } catch (Exception e) {
            exception = e;
        } finally {
            logNotificationEvent(request, startTime, exception);
        }
    }

    private void logNotificationEvent(NotificationRequest request, Instant startTime, Exception exception) {
        NotificationLogEntity logEntity = new NotificationLogEntity();
        notificationLogRepository.save(logEntity);
    }

    private void sendNotificationInternal(NotificationRequest request) {
        Optional<MessageTemplateEntity> template = findMessageTemplateByCode(request.getMessageTemplateCode());
        Notification notification = Notification.builder()
                .request(request)
                .media(request.getMedia())
                .recipient(request.getRecipient())
//                .messageTemplate(template)
//                .body(extractNotificationBody(template, request.getData()))
                .build();
        NotificationProvider provider = providers.get(request.getMedia());
        if (null == provider) {
            throw new NotificationProviderNotFoundException(request.getMedia().name());
        }

        provider.send(notification);
    }

    private String extractNotificationBody(MessageTemplate template, NotificationData data) {
        if (null == bodyProcessors)
            return null;
        for (Iterator<NotificationBodyProcessor> iterator = bodyProcessors.iterator(); iterator.hasNext(); ) {
            NotificationBodyProcessor bodyProcessor = iterator.next();
            String body = bodyProcessor.process(template, data);
            if (StringUtils.isNotEmpty(body)) {
                return body;
            }
        }
        return null;
    }

    private List<MessageTemplateEntity> findMessageTemplates() {
        if (null == messageTemplates) {
            messageTemplates = new ArrayList<>();
            messageTemplateRepository.findAll().forEach(messageTemplates::add);
        }
        return messageTemplates;
    }

    private Optional<MessageTemplateEntity> findMessageTemplateByCode(String code) {
        if (StringUtils.isEmpty(code)) {
            return Optional.empty();
        }
        return findMessageTemplates().stream()
                .filter(messageTemplate -> code.equals(messageTemplate.getCode()))
                .findFirst();
    }

}

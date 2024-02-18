package ir.daneshrefah.scm.notification.service;

import ir.daneshrefah.scm.common.model.notification.*;
import ir.daneshrefah.scm.notification.client.spec.NotificationService;
import ir.daneshrefah.scm.notification.domain.MessageTemplateEntity;
import ir.daneshrefah.scm.notification.domain.NotificationLogEntity;
import ir.daneshrefah.scm.notification.exception.NotificationProviderNotFoundException;
import ir.daneshrefah.scm.notification.repository.MessageTemplateRepository;
import ir.daneshrefah.scm.notification.repository.NotificationLogRepository;
import ir.daneshrefah.scm.notification.repository.mapper.MessageTemplateEntityMapper;
import ir.daneshrefah.scm.notification.service.provider.NotificationProvider;
import ir.daneshrefah.scm.notification.service.template.NotificationBodyProcessor;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ir.daneshrefah.scm.notification.config.ConfigProperties.SCM_NOTIFICATION_STATUS;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-30
 */
@Service
@ConditionalOnProperty(value = SCM_NOTIFICATION_STATUS,havingValue = "true")
public class NotificationServiceImpl implements NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final Map<NotificationMedia, NotificationProvider> providers;
    private final List<NotificationBodyProcessor> bodyProcessors;
    private final NotificationLogRepository notificationLogRepository;
    private final MessageTemplateRepository messageTemplateRepository;
    private List<MessageTemplateEntity> messageTemplates;
    private static final AtomicBoolean LOCK = new AtomicBoolean(false);

    public NotificationServiceImpl(List<NotificationProvider> providers, List<NotificationBodyProcessor> bodyProcessors,
                                   NotificationLogRepository notificationLogRepository,
                                   MessageTemplateRepository messageTemplateRepository) {
        this.providers = providers.stream().collect(Collectors.toMap(NotificationProvider::getType, Function.identity()));
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
        findMessageTemplateByCode(request.getMessageTemplateCode()).ifPresent(logEntity::setMessageTemplate);
        NotificationData data = request.getData();
        logEntity.setTerminalCode((String) data.get(DataKey.TERMINAL_CODE));
        logEntity.setUsername(request.getUsername());
        logEntity.setData(data);
        logEntity.setMedia(request.getMedia());
        logEntity.setRecipient(request.getRecipient());
        findMessageTemplateByCode(request.getMessageTemplateCode()).map(MessageTemplateEntity::getBody).ifPresent(logEntity::setBody);
        logEntity.setStatus(Objects.isNull(exception) ? NotificationStatus.DRAFT : NotificationStatus.FAILED);
        logEntity.setError(Objects.nonNull(exception) ? exception.getMessage() : null);
        notificationLogRepository.save(logEntity);
    }

    private void sendNotificationInternal(NotificationRequest request) {
        createNotification(request)
                .ifPresent(providers.computeIfAbsent(request.getMedia(), notificationMedia -> {
                    throw new NotificationProviderNotFoundException(request.getMedia().name());
                })::send);
    }

    private Optional<Notification> createNotification(NotificationRequest notificationRequest){
        Optional<MessageTemplateEntity> messageTemplateEntityOptional = findMessageTemplateByCode(notificationRequest.getMessageTemplateCode());
        if (messageTemplateEntityOptional.isPresent()) {
            MessageTemplateEntity template = messageTemplateEntityOptional.get();
            Notification notification = Notification.builder()
                    .request(notificationRequest)
                    .media(notificationRequest.getMedia())
                    .recipient(notificationRequest.getRecipient())
                    .messageTemplate(MessageTemplateEntityMapper.INSTANCE.toDto(template))
                    .body(extractNotificationBody(MessageTemplateEntityMapper.INSTANCE.toDto(template), notificationRequest.getData()))
                    .expiration(notificationRequest.getExpiration())
                    .build();
            return Optional.of(notification);
        }
        return Optional.empty();
    }

    private String extractNotificationBody(MessageTemplate template, NotificationData data) {
        if (Objects.isNull(bodyProcessors))
            return null;
        for (NotificationBodyProcessor bodyProcessor : bodyProcessors) {
            String body = bodyProcessor.process(template, data);
            if (StringUtils.isNotEmpty(body)) {
                return body;
            }
        }
        return null;
    }

    private List<MessageTemplateEntity> findMessageTemplates() {
            if (null == messageTemplates && !LOCK.get()) {
                  LOCK.set(true);
                  messageTemplates = new ArrayList<>();
                  messageTemplateRepository.findAll().forEach(messageTemplates::add);
                  LOCK.set(false);
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

package ir.daneshrefah.scm.notification.client.service;

import ir.daneshrefah.scm.common.model.notification.MessageTemplate;
import ir.daneshrefah.scm.common.model.notification.NotificationMessage;
import ir.daneshrefah.scm.common.model.notification.NotificationRequest;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.common.service.terminal.TerminalService;
import ir.daneshrefah.scm.notification.client.exception.*;
import ir.daneshrefah.scm.notification.client.service.provider.NotificationMessageProvider;
import ir.daneshrefah.scm.notification.client.service.spec.NotificationService;
import ir.daneshrefah.scm.notification.client.service.template.NotificationBodyProcessor;
import ir.daneshrefah.scm.utils.string.StringUtils;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
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

    private final TerminalService terminalService;
    private final MessageTemplateService messageTemplateService;
    private final List<NotificationBodyProcessor> bodyProcessors;
    private final List<NotificationMessageProvider> messageProviders;

    public void sendNotification(NotificationRequest request) {
        try {
            validateNotificationRequest(request);
            NotificationMessage notificationMessage = buildNotification(request);
            sendNotificationInternal(notificationMessage);
        } catch (Exception e) {
            log.error("error on send notification", e);
        }
    }

    private void validateNotificationRequest(NotificationRequest request) {
        ValidationUtils.checkNull(request, () -> new EmptyNotificationRequestException(request, "request"));
        ValidationUtils.checkNull(request.getRecipient(), () -> new EmptyNotificationRequestException(request, "recipient"));
        ValidationUtils.checkBlankString(request.getRecipient().getAddress(), () -> new EmptyNotificationRequestException(request, "recipient.address"));
        ValidationUtils.checkNull(request.getTemplate(), () -> new EmptyNotificationRequestException(request, "templateCode"));
        ValidationUtils.checkNull(request.getMedia(), () -> new EmptyNotificationRequestException(request, "media"));
        ValidationUtils.checkNull(request.getUserLocale(), () -> new EmptyNotificationRequestException(request, "locale"));
    }


    private void sendNotificationInternal(NotificationMessage notificationMessage) {
        for (Iterator<NotificationMessageProvider> iterator = messageProviders.iterator(); iterator.hasNext(); ) {
            NotificationMessageProvider messageProvider = iterator.next();
            if (messageProvider.supports(notificationMessage)) {
                messageProvider.send(notificationMessage);
            }
        }
    }

    private NotificationMessage buildNotification(NotificationRequest notificationRequest) {
        MessageTemplate messageTemplate = messageTemplateService.findMessageTemplateByCodeAndLocale(notificationRequest.getTemplate(),notificationRequest.getUserLocale());
        Terminal issuerTerminal = terminalService.findTerminalByCode(notificationRequest.getTerminalCode()).orElse(null);
        ValidationUtils.checkNull(messageTemplate, () -> new InvalidNotificationRequestException(notificationRequest, "messageTemplate"));
        ValidationUtils.checkNull(issuerTerminal, () -> new InvalidNotificationRequestException(notificationRequest, "issuerTerminal"));
        return new NotificationMessage()
                .setRequest(notificationRequest)
                .setPayload(extractNotificationBody(messageTemplate, notificationRequest));
    }

    private String extractNotificationBody(MessageTemplate template, NotificationRequest request) {
        if (Objects.isNull(bodyProcessors) || bodyProcessors.isEmpty()) {
            throw new NotificationBodyProcessorDoesNotExistsException(request);
        }
        for (NotificationBodyProcessor bodyProcessor : bodyProcessors) {
            try {
                String body = bodyProcessor.process(template, request);
                if (StringUtils.isNotEmpty(body)) {
                    return body;
                }
            } catch (Exception e) {
                throw new NotificationBodyProcessException(request, template.getBody());
            }
        }
        throw new NotFoundSupportedBodyProcessorException(request, template.getCode().getValue());
    }

}

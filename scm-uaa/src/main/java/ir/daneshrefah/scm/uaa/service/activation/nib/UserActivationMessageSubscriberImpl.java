package ir.daneshrefah.scm.uaa.service.activation.nib;

import ir.daneshrefah.scm.cache.client.connector.QueueTemplate;
import ir.daneshrefah.scm.common.constant.TerminalType;
import ir.daneshrefah.scm.common.data.service.person.PersonService;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.uaa.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import static ir.daneshrefah.scm.uaa.utils.Constants.ACTIVATION_PUSH_SUB_QUEUE_NAME;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserActivationMessageSubscriberImpl implements UserActivationMessageSubscriber {

    private final PersonService personService;
    private final UserService userService;
    private final UserActivationService userActivationService;
    private final QueueTemplate queueTemplate;
    private final UserChannelActivationNotifierService userChannelActivationNotifierService;

    @Override
    @Scheduled(fixedRate = 5000)
    @Async("schedulerThreadPool")
    public void subscribe() {
        while (queueTemplate.hasAnyMessages(ACTIVATION_PUSH_SUB_QUEUE_NAME)) {
            try {
                queueTemplate
                        .pop(ACTIVATION_PUSH_SUB_QUEUE_NAME)
                        .ifPresent(message -> {
                            log.info("Received activation push message from queue: {}", ACTIVATION_PUSH_SUB_QUEUE_NAME);
                            String username = (String) message.getPayload();
                            String fromTerminal = message.getAttributes().get("fromTerminal");
                            TerminalType terminal = TerminalType.fromCode(fromTerminal).orElseThrow(() -> new RuntimeException("Invalid terminal code " + fromTerminal));
                            userService
                                    .findByNicknameAndLegacyTerminalId(username, terminal.getLegacyTerminalId().intValue())
                                    .stream().findFirst().ifPresent(user -> {
                                        GeneralPerson dbPerson = personService.findPersonByUsername(user.getPerson().getUsername()).orElseThrow(() -> new RuntimeException("User " + username + " not found in activation queue "));
                                        try {
                                            userActivationService.activate(dbPerson, terminal);
                                            userChannelActivationNotifierService.sendSuccessNotification(dbPerson, username,terminal,TerminalType.NIB);
                                        } catch (Exception e) {
                                            log.error("user channel activation failed for person.username :: {}: {}", dbPerson.getUsername(), safeMessage(e));
                                            userChannelActivationNotifierService.sendFailedNotification(dbPerson,username,terminal, TerminalType.NIB);
                                        }
                                    });
                        });
            } catch (Exception e) {
                log.error(">>> Error in subscribing message from queue {}: {}", ACTIVATION_PUSH_SUB_QUEUE_NAME, safeMessage(e));
            }
        }
    }

    private String safeMessage(Exception exception) {
        if (exception == null || exception.getMessage() == null) {
            return exception == null ? null : exception.getClass().getSimpleName();
        }
        String message = exception.getMessage()
                .replace('\r', ' ')
                .replace('\n', ' ')
                .replaceAll("(?i)(password|token|authorization|client_secret|authorization_code|pin|otp|session[_-]?id|card[_-]?number)\\s*[:=]\\s*\\S+", "$1=***")
                .trim();
        return message.length() > 300 ? message.substring(0, 300) : message;
    }
}

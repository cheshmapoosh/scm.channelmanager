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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import static ir.daneshrefah.scm.uaa.utils.Constants.ACTIVATION_PUSH_SUB_QUEUE_NAME;

@Service
@ConditionalOnProperty(
        prefix = "scm.uaa.activation.nib",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
@RequiredArgsConstructor
@Slf4j
public class UserActivationMessageSubscriberImpl implements UserActivationMessageSubscriber {

    private final PersonService personService;
    private final UserService userService;
    private final NibActivationService nibActivationService;
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
                                        GeneralPerson dbPerson = personService.findPersonByUsername(user.getPerson().getUsername())
                                                .orElseThrow(() -> new IllegalStateException("Activation queue user was not found"));
                                        try {
                                            nibActivationService.activate(dbPerson, terminal);
                                            userChannelActivationNotifierService.sendSuccessNotification(dbPerson, username,terminal,TerminalType.NIB);
                                        } catch (Exception e) {
                                            log.error("NIB user channel activation failed; errorType={}", errorType(e));
                                            userChannelActivationNotifierService.sendFailedNotification(dbPerson,username,terminal, TerminalType.NIB);
                                        }
                                    });
                        });
            } catch (Exception e) {
                log.error("Error while processing queue {}; errorType={}",
                        ACTIVATION_PUSH_SUB_QUEUE_NAME, errorType(e));
            }
        }
    }

    private String errorType(Exception exception) {
        return exception == null ? "Unknown" : exception.getClass().getSimpleName();
    }
}

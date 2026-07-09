package ir.daneshrefah.scm.uaa.service.activation.nib;

import ir.daneshrefah.scm.cache.starter.connector.QueueTemplate;
import ir.daneshrefah.scm.cache.starter.model.Message;
import ir.daneshrefah.scm.common.constant.TerminalType;
import ir.daneshrefah.scm.common.data.service.person.PersonService;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.uaa.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;

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
public class UserActivationMessageSubscriberImpl implements UserActivationMessageSubscriber, SmartLifecycle {

    private static final long ERROR_BACKOFF_MILLIS = 5000;

    private final PersonService personService;
    private final UserService userService;
    private final NibActivationService nibActivationService;
    private final QueueTemplate queueTemplate;
    private final UserChannelActivationNotifierService userChannelActivationNotifierService;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread consumerThread;

    @Override
    public void start() {
        if (running.compareAndSet(false, true)) {
            consumerThread = new Thread(this::consumeMessages, "uaa-nib-activation-queue-consumer");
            consumerThread.setDaemon(true);
            consumerThread.start();
        }
    }

    @Override
    public void stop() {
        running.set(false);
        Thread thread = consumerThread;
        if (thread != null) {
            thread.interrupt();
        }
    }

    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void subscribe() {
        try {
            queueTemplate
                    .<String>take(ACTIVATION_PUSH_SUB_QUEUE_NAME)
                    .ifPresent(this::processMessage);
        } catch (Exception e) {
            log.error("Error while processing queue {}; errorType={}",
                    ACTIVATION_PUSH_SUB_QUEUE_NAME, errorType(e));
            pauseAfterError();
        }
    }

    private void consumeMessages() {
        while (running.get() && !Thread.currentThread().isInterrupted()) {
            subscribe();
        }
    }

    private void processMessage(Message<String> message) {
        log.info("Received activation push message from queue: {}", ACTIVATION_PUSH_SUB_QUEUE_NAME);
        String username = message.getPayload();
        String fromTerminal = message.getAttributes().get("fromTerminal");
        TerminalType terminal = TerminalType.fromCode(fromTerminal).orElseThrow(() -> new RuntimeException("Invalid terminal code " + fromTerminal));
        userService
                .findByNicknameAndLegacyTerminalId(username, terminal.getLegacyTerminalId().intValue())
                .stream().findFirst().ifPresent(user -> {
                    GeneralPerson dbPerson = personService.findPersonByUsername(user.getPerson().getUsername())
                            .orElseThrow(() -> new IllegalStateException("Activation queue user was not found"));
                    try {
                        nibActivationService.activate(dbPerson, terminal);
                        userChannelActivationNotifierService.sendSuccessNotification(dbPerson, username, terminal, TerminalType.NIB);
                    } catch (Exception e) {
                        log.error("NIB user channel activation failed; errorType={}", errorType(e));
                        userChannelActivationNotifierService.sendFailedNotification(dbPerson, username, terminal, TerminalType.NIB);
                    }
                });
    }

    private void pauseAfterError() {
        try {
            Thread.sleep(ERROR_BACKOFF_MILLIS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String errorType(Exception exception) {
        return exception == null ? "Unknown" : exception.getClass().getSimpleName();
    }
}

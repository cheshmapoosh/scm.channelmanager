package ir.daneshrefah.scm.plugin.api.inbound;

import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceChannelAccess;
import ir.daneshrefah.scm.logging.api.EventProducer;
import ir.daneshrefah.scm.logging.domain.event.Event;
import ir.daneshrefah.scm.logging.domain.event.MessageBuildEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-03
 */
public abstract class MessageBuilder<T> {

    protected static final Logger LOGGER = LoggerFactory.getLogger(MessageBuilder.class);

    public final Message build(T input, TerminalServiceChannelAccess service) {
        Object body = extractBody(input);
        Instant startTime = Instant.now();

        Message result = buildInternal(input, body, service);

        logMessageGenerationEvent(result, startTime, body);

        return result;
    }

    protected abstract Object extractBody(T input);

    protected abstract Message buildInternal(T input, Object body, TerminalServiceChannelAccess service);

    private void logMessageGenerationEvent(Message message, Instant startTime, Object input) {
        Instant endTime = Instant.now();
        Event event = MessageBuildEvent.builder()
                .correlationId(message.getHeader().getCorrelationId())
                .clientCorrelationId(message.getHeader().getClientCorrelationId())
                .startTimestamp(startTime)
//                .input(input)
                .threadName(Thread.currentThread().getName())
                .sourceClassName(this.getClass().getSimpleName())
                .clientAgent(message.getHeader().getClientAgent())
                .serverHost(message.getHeader().getServerHost())
                .terminalCode(message.getHeader().getTerminalCode())
                .endTimestamp(endTime)
                .durationMillis(Duration.between(startTime, endTime).toMillis())
                .output(message)
                .build();
        EventProducer.getInstance().sendEvent(event);
    }

}

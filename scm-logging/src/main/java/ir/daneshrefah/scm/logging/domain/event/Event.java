package ir.daneshrefah.scm.logging.domain.event;

import ir.daneshrefah.scm.common.model.message.MessageStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-06
 */
@SuperBuilder
@Getter
public abstract class Event {

    private final String correlationId;
    private final String messageId;
    private final String parentMessageId;
    private final int level;
    private final String terminalCode;
    private final String channelCode;
    private final String username;
    private final String cspUsername;
    private final Exception error;
    private final String exceptionClassName;
    private final String threadName;
    private final Instant startTime;

    public abstract EventType getEventType();


}

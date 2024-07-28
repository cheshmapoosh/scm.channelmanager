package ir.daneshrefah.scm.logging.domain.event;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.model.error.Error;
import ir.daneshrefah.scm.common.model.message.MessageInput;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-07-21
 */
@SuperBuilder
@Getter
public class InboundEvent extends Event {

    private final String channelClassName;
    private final MessageStatus messageStatus;
    private final List<Error> errors;
    private final JsonNode response;
    private final MessageInput messageInput;
    private final Instant startTime;
    private final Instant endTime;

    @Override
    public EventType getEventType() {
        return EventType.INBOUND;
    }

    public Long getDurationMillis() {
        if (Objects.isNull(startTime) || Objects.isNull(endTime)) {
            return null;
        }
        return Duration.between(startTime, endTime).toMillis();
    }

}

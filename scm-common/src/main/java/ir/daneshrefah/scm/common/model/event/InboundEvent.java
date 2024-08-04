package ir.daneshrefah.scm.common.model.event;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.model.error.Error;
import ir.daneshrefah.scm.common.model.message.MessageInput;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
@NoArgsConstructor
public class InboundEvent extends Event {

    private String channelClassName;
    private MessageStatus messageStatus;
    private List<Error> errors;
    private JsonNode response;
    private MessageInput messageInput;
    private Instant startTime;
    private Instant endTime;

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

package ir.daneshrefah.scm.common.model.event;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.model.event.constants.EventType;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-04-07
 */
@SuperBuilder
@Getter
@NoArgsConstructor
public class ServiceEvent extends Event {

    private JsonNode request;
    private JsonNode response;
    private MessageStatus status;
    private Exception exception;
    private Instant startTime;
    private Instant endTime;

    public long getDurationMillis() {
        return Duration.between(startTime, endTime).toMillis();
    }

    public String getExceptionClassName() {
        return Objects.isNull(exception) ? null : exception.getClass().getName();
    }

    @Override
    public EventType getEventType() {
        return EventType.SERVICE_CALL;
    }

}

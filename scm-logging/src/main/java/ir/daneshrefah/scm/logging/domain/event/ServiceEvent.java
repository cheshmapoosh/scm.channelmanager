package ir.daneshrefah.scm.logging.domain.event;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import lombok.Getter;
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
public class ServiceEvent extends Event {

    private final JsonNode request;
    private final JsonNode response;
    private final MessageStatus status;
    private final Exception exception;
    private final Instant startTime;
    private final Instant endTime;

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

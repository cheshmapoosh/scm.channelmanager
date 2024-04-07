package ir.daneshrefah.scm.logging.domain.event;

import ir.daneshrefah.scm.common.model.message.MessageStatus;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

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

    private final String serviceCode;
    private final String request;
    private final String response;
    private final MessageStatus status;
    private final Instant endTime;
    private final Long durationMillis;

    @Override
    public EventType getEventType() {
        return EventType.SERVICE_CALL;
    }

}

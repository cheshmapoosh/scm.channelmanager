package ir.daneshrefah.scm.logging.domain.event;

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
public class OutboundEvent extends Event {

    private final String providerCode;
    private final String providerTargetUrl;
    private final String providerResponseCode;
    private final String request;
    private final String response;
    private final Instant endTime;
    private final Long durationMillis;

    @Override
    public EventType getEventType() {
        return EventType.OUTBOUND;
    }

}

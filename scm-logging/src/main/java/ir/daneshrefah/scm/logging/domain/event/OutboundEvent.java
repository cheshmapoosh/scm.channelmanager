package ir.daneshrefah.scm.logging.domain.event;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
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
public class OutboundEvent extends Event {

    private final String providerClassName;
    private final String providerCode;
    private final String providerTargetUrl;
    private final String providerProtocol;
    private final String providerResponseCode;
    private final Object requestBody;
    private final String requestBodyType;
    private final Map<String, Object> requestHeaders;
    private final String responseBody;
    private final String responseBodyType;
    private final Map<String, Object> responseHeaders;
    private final Exception exception;
    private final Instant startTime;
    private final Instant endTime;

    @Override
    public EventType getEventType() {
        return EventType.OUTBOUND;
    }

    public long getDurationMillis() {
        return Duration.between(startTime, endTime).toMillis();
    }

    public String getExceptionClassName() {
        return Objects.isNull(exception) ? null : exception.getClass().getName();
    }

}

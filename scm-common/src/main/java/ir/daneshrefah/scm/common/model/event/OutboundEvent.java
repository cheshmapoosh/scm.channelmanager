package ir.daneshrefah.scm.common.model.event;

import ir.daneshrefah.scm.common.model.event.constants.EventType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
@Setter
@NoArgsConstructor
public class OutboundEvent extends Event {

    private String providerClassName;
    private String providerCode;
    private String providerTargetUrl;
    private String providerProtocol;
    private String providerResponseCode;
    private Object requestBody;
    private String requestBodyType;
    private Map<String, Object> requestHeaders;
    private String responseBody;
    private String responseBodyType;
    private Map<String, Object> responseHeaders;
    private Exception exception;
    private Instant startTime;
    private Instant endTime;

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

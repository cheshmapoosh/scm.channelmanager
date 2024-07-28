package ir.daneshrefah.scm.common.model.message;

import ir.daneshrefah.scm.common.model.service.ServiceProviderProtocol;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.Map;
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
public abstract class MessageOutput {

    private final Instant startTime = Instant.now();
    @Setter
    private String externalCorrelationId;
    @Setter
    private Object body;
    @Setter
    private String providerUrl;
    @Setter
    private Map<String, Object> headers;

    public <T> T getBody(Class<T> type) {
        if (Objects.isNull(body)) {
            return null;
        }
        if (type.isInstance(body)) {
            return (T) body;
        }
        if (String.class.equals(type)) {
            return (T) body.toString();
        }
        return null;
    }

    public String getBodyType() {
        if (Objects.isNull(body)) {
            return "null";
        }
        return body.getClass().getName();
    }

    public abstract ServiceProviderProtocol getProtocol();
}

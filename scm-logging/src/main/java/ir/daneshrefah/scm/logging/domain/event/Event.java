package ir.daneshrefah.scm.logging.domain.event;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-06
 */
@Getter
@SuperBuilder
public abstract class Event<T> {

    private String correlationId;
    private String clientCorrelationId;
    private Instant startTimestamp;
    private T input;
    private String threadName;
    private String sourceClassName;
    private String clientAgent;
    private String serverHost;
    private String terminalCode;

    public abstract EventType getType();

}

package ir.daneshrefah.scm.logging.domain.event;

import ir.daneshrefah.scm.common.model.message.MessageStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-06
 */
@Getter
@Builder
public class Event {

    private EventType type;
    private MessageStatus status;
    private String correlationId;
    private String source;
    private String terminalCode;
    private String channelCode;
    private Instant startTime;
    private Instant endTime;
    private Long durationMillis;
    private String sourceClassName;
    private String threadName;
    private Object input;
    private Object output;
    private Exception error;

}

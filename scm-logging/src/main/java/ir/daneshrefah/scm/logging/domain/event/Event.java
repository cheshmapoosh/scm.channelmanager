package ir.daneshrefah.scm.logging.domain.event;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-06
 */
@SuperBuilder
@Getter
public abstract class Event {

    private final String terminalCode;
    private final String channelCode;
    private final String clientId;

    private final String correlationId;
    private final String clientCorrelationId;
    private final String clientFlowId;

    private final String serviceCode;

    private final String username;
    private final String nickname;
    private final String delegatorUsername;
    private final String delegatorNickname;

    private final String messageId;
    private final String threadName;
    private final String hostAddress;

    public abstract EventType getEventType();


}

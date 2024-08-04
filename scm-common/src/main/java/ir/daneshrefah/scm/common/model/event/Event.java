package ir.daneshrefah.scm.common.model.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = InboundEvent.class, name = "InboundEvent"),
        @JsonSubTypes.Type(value = OutboundEvent.class, name = "OutboundEvent"),
        @JsonSubTypes.Type(value = ServiceEvent.class, name = "ServiceEvent"),
})
@NoArgsConstructor
public abstract class Event {

    private String terminalCode;
    private String channelCode;
    private String clientId;

    private String correlationId;
    private String clientCorrelationId;
    private String clientFlowId;

    private String serviceCode;

    private String username;
    private String nickname;
    private String delegatorUsername;
    private String delegatorNickname;

    private String messageId;
    private String threadName;
    private String hostAddress;

    public abstract EventType getEventType();

    public int getVersion() {return 1;}
}

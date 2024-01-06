package ir.daneshrefah.scm.logging.domain.event;

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

    private String correlationId;
    private String clientCorrelationId;
    private Instant timestamp;
    private String username;
    private EventType type;
    private EventPhase phase;
    private String terminalCode;
    private String clientId;
    private String threadName;
    private String assetIdentifier;
    private String sourceIdentifier;
    private String sourceClassName;
    private String accessParameter;
    private Object data;
    private String serverHost;
    private String targetUrl;
    private String clientAgent;
    private String clientUrl;
//    private String loginAuthenticationMethod;
//    private String transactionAuthenticationMethod;


}

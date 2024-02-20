package ir.daneshrefah.scm.common.model.message;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-03
 */
@Builder
@Getter
public class MessageRequestInfo implements Serializable {

    private final MessageInput input;
    private final String terminalCode;
    private final String clientId;
    private final String serviceCode;
    private final String contentType;
    private final String clientRemoteAddress;
    private final String clientCorrelationId;
    private final Instant clientTimestamp;
    private final String clientAgent;
    private final String accessParameter;
    private final String username;
    private final ClientAuthenticationType authenticationType;
    private final String authenticationValue;
    private final ClientAuthenticationType transactionAuthenticationType;
    private final String transactionAuthenticationValue;
    private final Instant receiveTimestamp;
    private final String serverHost;
    private final JsonNode payload;
    private final boolean isForCheck;
    private final Exception error;

}

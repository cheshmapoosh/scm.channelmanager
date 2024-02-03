package ir.daneshrefah.scm.common.model.message;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.io.Serializable;
import java.time.Instant;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-16
 */
@Data
public class MessageBuildRequest<T extends MessageInput> implements Serializable {

    private T input;
    private String terminalCode;
    private String clientId;
    private String serviceCode;
    private String contentType;
    private String clientRemoteAddress;
    private String clientCorrelationId;
    private Instant clientTimestamp;
    private String clientAgent;
    private String accessParameter;
    private String username;
    private ClientAuthenticationType authenticationType;
    private String authenticationValue;
    private ClientAuthenticationType transactionAuthenticationType;
    private String transactionAuthenticationValue;
    private Instant receiveTimestamp;
    private String serverHost;
    private JsonNode payload;
    private boolean isForCheck;
    private Exception error;

}

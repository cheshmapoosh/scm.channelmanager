package ir.daneshrefah.scm.common.model.message;

import com.fasterxml.jackson.databind.JsonNode;
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
@Getter
public class MessageRequestInfo<T extends MessageInput> implements Serializable {

    public MessageRequestInfo(MessageBuildRequest<T> request) {
        this.input = null != request ? request.getInput() : null;
        this.terminalCode = null != request ?  request.getTerminalCode() : null;
        this.clientId = null != request ?  request.getClientId() : null;
        this.serviceCode = null != request ?  request.getServiceCode() : null;
        this.contentType = null != request ?  request.getContentType() : null;
        this.clientRemoteAddress = null != request ?  request.getClientRemoteAddress() : null;
        this.clientCorrelationId = null != request ?  request.getClientCorrelationId() : null;
        this.clientTimestamp = null != request ?  request.getClientTimestamp() : null;
        this.clientAgent = null != request ?  request.getClientAgent() : null;
        this.accessParameter = null != request ?  request.getAccessParameter() : null;
        this.username = null != request ?  request.getUsername() : null;
        this.authenticationType = null != request ?  request.getAuthenticationType() : null;
        this.authenticationValue = null != request ?  request.getAuthenticationValue() : null;
        this.transactionAuthenticationType = null != request ?  request.getTransactionAuthenticationType() : null;
        this.transactionAuthenticationValue = null != request ?  request.getTransactionAuthenticationValue() : null;
        this.receiveTimestamp = null != request ?  request.getReceiveTimestamp() : null;
        this.serverHost = null != request ?  request.getServerHost() : null;
        this.payload = null != request ?  request.getPayload() : null;
        this.isForCheck = null != request ?  request.isForCheck() : false;
        this.error = null != request ?  request.getError() : null;
    }

    private final T input;
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

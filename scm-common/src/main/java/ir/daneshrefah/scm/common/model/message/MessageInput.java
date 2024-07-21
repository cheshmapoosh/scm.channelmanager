package ir.daneshrefah.scm.common.model.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ir.daneshrefah.scm.common.model.terminal.Channel;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.*;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-03
 */
@SuperBuilder
@Getter
public abstract class MessageInput<T> {

    private final String correlationId = UUID.randomUUID().toString();
    private final Map<String, Object> headers;
    private final String serviceCode;
    private final String terminalCode;
    @JsonIgnore
    private final Terminal terminal;
    @JsonIgnore
    private final Channel channel;
    private final T body;
    private final String contentType;
    private final Instant receiveTimestamp = Instant.now();
    private final String serverHost;
    private final boolean isForCheck;
    private final String clientId;
    private final String clientCorrelationId;
    private final String clientFlowId;
    private final Instant clientTimestamp;
    private final String accessParameter;
    private final String username;
    private final ClientAuthenticationType authenticationType;
    private final String authenticationValue;
    private final ClientAuthenticationType transactionAuthenticationType;
    private final String transactionAuthenticationValue;

    public String getHeader(String key) {
        return null != headers ? (String) headers.get(key) : null;
    }

}

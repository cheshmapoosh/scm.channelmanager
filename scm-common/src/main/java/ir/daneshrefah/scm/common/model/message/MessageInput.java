package ir.daneshrefah.scm.common.model.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ir.daneshrefah.scm.common.model.terminal.Channel;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.apache.camel.tracing.SpanAdapter;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-03
 */
@SuperBuilder
@Getter
@NoArgsConstructor
public abstract class MessageInput<T> {
    @Builder.Default
    private final String correlationId = UUID.randomUUID().toString();
    private Map<String, Object> headers;
    private String serviceCode;
    private String terminalCode;
    @JsonIgnore
    private Terminal terminal;
    @JsonIgnore
    private Channel channel;
    private T body;
    private String contentType;
    @Builder.Default
    private Instant receiveTimestamp = Instant.now();
    private String serverHost;
    private boolean isForCheck;
    private String clientId;
    private String clientCorrelationId;
    private String clientFlowId;
    private String flowId;
    private Instant clientTimestamp;
    private String accessParameter;
    private String username;
    private TokenType authenticationType;
    private String authenticationValue;
    private TokenType transactionAuthenticationType;
    private String transactionAuthenticationValue;
    private SpanAdapter spanAdapter;
    public String getHeader(String key) {
        return null != headers ? (String) headers.get(key) : null;
    }

}

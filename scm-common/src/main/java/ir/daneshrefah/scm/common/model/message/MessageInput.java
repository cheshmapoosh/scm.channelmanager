package ir.daneshrefah.scm.common.model.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import ir.daneshrefah.scm.common.model.terminal.Channel;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AbstractExternalMessageInput.class, name = "AbstractExternalMessageInput"),
        @JsonSubTypes.Type(value = AbstractInternalMessageInput.class, name = "AbstractInternalMessageInput"),
        @JsonSubTypes.Type(value = HttpMessageInput.class, name = "HttpMessageInput"),
        @JsonSubTypes.Type(value = JobMessageInput.class, name = "JobMessageInput"),
        @JsonSubTypes.Type(value = ProcessMessageInput.class, name = "ProcessMessageInput")
})
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
    public String getHeader(String key) {
        return null != headers ? (String) headers.get(key) : null;
    }

}

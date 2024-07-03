package ir.daneshrefah.scm.common.model.message;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    private final Map<String, Object> headers;
    private final String serviceCode;
    private final String terminalCode;
    private final String channelCode;
    private final T body;
    private final String contentType;
//    private final String authorization;
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
    private List<Exception> exceptionList;

    public String getHeader(String key) {
        return null != headers ? (String) headers.get(key) : null;
    }

    public void addException(Exception exception) {
        if (Objects.isNull(exception)) {
            return;
        }
        if (Objects.isNull(exceptionList)) {
            exceptionList = new ArrayList<>();
        }
        exceptionList.add(exception);
    }

}

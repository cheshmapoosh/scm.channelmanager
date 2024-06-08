package ir.daneshrefah.scm.common.model.message;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.Map;

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
    private final String authorization;
    private final Instant receiveTimestamp = Instant.now();
    private final String serverHost;
    private final boolean isForCheck;

    public String getHeader(String key) {
        return null != headers ? (String) headers.get(key) : null;
    }

}

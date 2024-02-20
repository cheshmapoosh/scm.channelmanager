package ir.daneshrefah.scm.common.model.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-03
 */
@RequiredArgsConstructor
@Getter
public abstract class MessageInput {

    private final Map<String, Object> headers;
    private final String serviceCode;
    private final String body;
    private final String contentType;
    private final String clientRemoteAddress;
    private final String clientAgent;
    private final String authorization;
    private final Instant receiveTimestamp = Instant.now();
    private final String serverHost;
    private final boolean isForCheck;

    public String getHeader(String key) {
        return null != headers ? (String) headers.get(key) : null;
    }

}

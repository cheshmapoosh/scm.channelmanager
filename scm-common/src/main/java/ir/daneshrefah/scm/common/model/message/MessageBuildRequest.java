package ir.daneshrefah.scm.common.model.message;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-16
 */
@Getter
@Builder
public class MessageBuildRequest {

    private String terminalCode;
    private String channelCode;
    private String contentType;
    private String clientCorrelationId;
    private Instant clientTimestamp;
    private Instant receiveTimestamp;
    private String accessParameter;
    private String clientAgent;
    private String serverHost;
    private String clientAddress;
    private JsonNode payload;
    private boolean isForCheck;

}

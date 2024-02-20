package ir.daneshrefah.scm.common.model.message;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-03
 */
@Getter
public class HttpMessageInput extends MessageInput {

    private final String httpUrl;
    private final String httpMethod;

    @Builder
    private HttpMessageInput(Map<String, Object> headers, String serviceCode, String body, String contentType, String clientRemoteAddress, String clientAgent, String authorization, String serverHost, boolean isForCheck, String httpUrl, String httpMethod) {
        super(headers, serviceCode, body, contentType, clientRemoteAddress, clientAgent, authorization, serverHost, isForCheck);
        this.httpUrl = httpUrl;
        this.httpMethod = httpMethod;
    }

}

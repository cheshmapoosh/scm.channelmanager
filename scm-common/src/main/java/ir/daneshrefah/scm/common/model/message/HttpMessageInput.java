package ir.daneshrefah.scm.common.model.message;

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

    private final String url;
    private final String method;

    public HttpMessageInput(Map<String, Object> headers, String body, String url, String method) {
        super(headers, body);
        this.url = url;
        this.method = method;
    }
}

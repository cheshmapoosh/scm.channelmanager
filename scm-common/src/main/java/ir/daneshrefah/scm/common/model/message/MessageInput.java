package ir.daneshrefah.scm.common.model.message;

import lombok.Getter;

import java.util.Collections;
import java.util.Map;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-03
 */
@Getter
public abstract class MessageInput {

    private final Map<String, Object> headers;
    private final String body;

    protected MessageInput(Map<String, Object> headers, String body) {
        this.headers = Collections.unmodifiableMap(headers);
        this.body = body;
    }

    public String getHeader(String key) {
        return null != headers ? (String) headers.get(key) : null;
    }

}

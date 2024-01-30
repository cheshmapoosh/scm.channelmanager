package ir.daneshrefah.scm.common.model.message;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import ir.daneshrefah.scm.common.model.error.Error;
import ir.daneshrefah.scm.common.model.error.ErrorCodes;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
@Getter
@Builder
public class Message implements Serializable {

    private Header header;
    private Status status;
    private JsonNode payload;
    private List<Error> errors;

    public void addError(Error error, Status status) {
        if (null == errors)
            errors = new ArrayList<>();
        errors.add(error);
        this.status = status;
        nullPayload();
    }

    public void addAccessDeniedError(String source, Integer errorCode, String message) {
        addError(new Error(source, null != errorCode ? errorCode : ErrorCodes.ERROR_CODE_ACCESS_DENIED,
                        null != message ? message : "access denied."),
                Status.SC_ACCESS_DENIED);
    }

    public void nullPayload() {
        this.payload = JsonNodeFactory.instance.nullNode();
    }

    public void payload(JsonNode payload) {
        this.payload = null != payload ? payload : JsonNodeFactory.instance.nullNode();
    }

    public void status(Status status) {
        if (null != status) {
            this.status = status;
        }
    }

    public String getPayloadValue(String property) {
        if (null == property || null == payload || payload.isNull())
            return null;
        return (payload.has(property) && payload.get(property).isTextual()) ? payload.get(property).asText() : null;
    }

    public Integer getIntegerPayloadValue(String property) {
        if (null == property || null == payload || payload.isNull())
            return null;
        return (payload.has(property) && payload.get(property).isInt()) ? payload.get(property).asInt() : null;
    }

}

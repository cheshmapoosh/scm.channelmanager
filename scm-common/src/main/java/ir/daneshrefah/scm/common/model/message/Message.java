package ir.daneshrefah.scm.common.model.message;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import ir.daneshrefah.scm.common.model.error.Error;
import ir.daneshrefah.scm.common.model.error.ErrorCodes;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

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
@Setter
@RequiredArgsConstructor
public class Message implements Serializable {
    private Header header;
    private Status status;
    private List<Error> errors;
    private JsonNode payload;
    private final MessageBuildRequest request;

    public void nullPayload() {
        this.payload = JsonNodeFactory.instance.nullNode();
    }

    public void addError(Error error, Status status) {
        if (null == errors)
            errors = new ArrayList<>();
        errors.add(error);
        setStatus(status);
    }

    public void addAccessDeniedError(String source, Integer errorCode, String message) {
        addError(new Error(source, null != errorCode ? errorCode : ErrorCodes.ERROR_CODE_ACCESS_DENIED,
                        null != message ? message : "access denied."),
                Status.SC_ACCESS_DENIED);
        setPayload(JsonNodeFactory.instance.nullNode());
    }

    public void addErrors(List<Error> errors) {
        if (null == errors)
            return;

        if (null == this.errors)
            this.errors = new ArrayList<>();
        this.errors.addAll(errors);
    }

    public String getPayloadValue(String property) {
        if (null == property || null == payload || payload.isNull())
            return null;
        return (payload.has(property) && payload.get(property).isTextual()) ? payload.get(property).asText() : null;
    }
}

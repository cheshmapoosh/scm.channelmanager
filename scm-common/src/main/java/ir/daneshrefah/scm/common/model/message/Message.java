package ir.daneshrefah.scm.common.model.message;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.common.exception.MessagePayloadMergeException;
import ir.daneshrefah.scm.common.model.error.Error;
import ir.daneshrefah.scm.common.model.error.ErrorCodes;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
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
    private MessageStatus status;
    private JsonNode payload;
    private List<Error> errors;

    public void addError(Error error, MessageStatus status) {
        if (null == errors)
            errors = new ArrayList<>();
        errors.add(error);
        this.status = status;
        nullPayload();
    }

    public void addErrors(List<Error> errors, MessageStatus status) {
        if (null == this.errors) {
            this.errors = errors;
        } else {
            this.errors.addAll(errors);
        }
        this.status = status;
        nullPayload();
    }

    public void addAccessDeniedError(String source, Integer errorCode, String message) {
        addError(new Error(source, null != errorCode ? errorCode : ErrorCodes.ERROR_CODE_ACCESS_DENIED,
                        null != message ? message : "access denied."),
                MessageStatus.SC_ACCESS_DENIED);
    }

    public void nullPayload() {
        this.payload = JsonNodeFactory.instance.nullNode();
    }

    public Message payload(JsonNode payload) {
        this.payload = null != payload ? payload : JsonNodeFactory.instance.nullNode();
        return this;
    }

    public void status(MessageStatus status) {
        if (null != status) {
            this.status = status;
        }
    }

    public void appendPayload(JsonNode newPayload) {
        if (null == newPayload || newPayload.isNull() || newPayload.isEmpty()) {
            return;
        }
        if (null == payload || payload.isNull() || payload.isEmpty()) {
            payload = newPayload;
            return;
        }
        if ((payload.isArray() && !newPayload.isArray()) || (!payload.isArray() && newPayload.isArray())) {
            throw new MessagePayloadMergeException("incompatible array type of payloads", newPayload, null);
        }
        if (payload.isObject()) {
            for (Iterator<String> it = newPayload.fieldNames(); it.hasNext(); ) {
                String filed = it.next();
                ((ObjectNode) payload).set(filed, newPayload.get(filed).deepCopy());
            }
        } else if (payload.isArray()) {
            for (JsonNode element : newPayload) {
                ((ArrayNode) payload).add(element.deepCopy());
            }
        }
    }

    public boolean hasNonNullProperty(String propertyName) {
        if (StringUtils.isEmpty(propertyName)) {
            return false;
        }
        if (null == payload || payload.isNull()) {
            return false;
        }
        return payload.hasNonNull(propertyName);
    }

    public boolean hasNonBlankProperty(String propertyName) {
        return hasNonNullProperty(propertyName) && !payload.get(propertyName).isEmpty();
    }

    public void setPayloadValue(String property, String value) {
        if (null == property || null == value) {
            return;
        }
        if (null == payload || payload.isNull()) {
            payload = JsonNodeFactory.instance.objectNode();
        }
        ((ObjectNode) payload).put(property, value);
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

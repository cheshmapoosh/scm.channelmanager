package ir.daneshrefah.scm.common.model.message;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import ir.daneshrefah.scm.common.model.error.Error;
import ir.daneshrefah.scm.common.model.error.ErrorReason;
import ir.daneshrefah.scm.common.model.error.ErrorType;

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
public class Message implements Serializable {
    private Header header;
    private Status status;
    private List<Error> errors;
    private JsonNode payload;

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<Error> getErrors() {
        return errors;
    }

    public void setErrors(List<Error> errors) {
        this.errors = errors;
    }

    public JsonNode getPayload() {
        return payload;
    }

    public void setPayload(JsonNode payload) {
        this.payload = payload;
    }

    public void addError(Error error, Status status) {
        if (null == errors)
            errors = new ArrayList<>();
        errors.add(error);
        setStatus(status);
    }

    public void addAccessDeniedError(String source) {
        addError(new Error(ErrorType.ACCESS_DENIED, source,
                ErrorReason.IS_INVALID), Status.SC_ACCESS_DENIED);
        setPayload(JsonNodeFactory.instance.nullNode());
    }

    public void addErrors(List<Error> errors) {
        if (null == errors)
            return;

        if (null == this.errors)
            this.errors = new ArrayList<>();
        this.errors.addAll(errors);
    }

}

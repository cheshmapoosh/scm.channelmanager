package ir.daneshrefah.scm.plugin.api.model.message;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
public class Message {
    private Header header;
    private Status status;
    private List<Error> errors;
    private Object payload;
    private MessageComponent messageComponent;

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

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public MessageComponent getMessageComponent() {
        return messageComponent;
    }

    public void setMessageComponent(MessageComponent messageComponent) {
        this.messageComponent = messageComponent;
    }
}

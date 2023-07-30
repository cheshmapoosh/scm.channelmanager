package ir.daneshrefah.scm.plugin.api.model.message;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private List<Event> events;
    private JsonNode payload;
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

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public JsonNode getPayload() {
        return payload;
    }

    public void setPayload(JsonNode payload) {
        this.payload = payload;
    }

    public MessageComponent getMessageComponent() {
        return messageComponent;
    }

    public void setMessageComponent(MessageComponent messageComponent) {
        this.messageComponent = messageComponent;
    }

    public void addError(Error error, String statusCode) {
        if (null == errors)
            errors = new ArrayList<>();
        errors.add(error);
        setStatus(Status.findByCode(statusCode));
    }

    public void addEvent(EventType type, LocalDateTime startTime, LocalDateTime endTime, String providerCode) {
        if (null == events)
            events = new ArrayList<>();
        Event event = new Event();
        event.setType(type);
        event.setStartTime(startTime);
        event.setEndTime(endTime);
        event.setDurationMillis(Duration.between(startTime, endTime).toMillis());
        event.setServiceProviderCode(providerCode);
        events.add(event);
    }
}

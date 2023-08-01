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

    public void addTransformEvent(LocalDateTime startTime, LocalDateTime endTime, String transformerClass,
                                  boolean isSuccessful, String errorMessage, String outputType) {

        TransformEvent event = (TransformEvent) addEvent(EventType.TRANSFORM, startTime, endTime, isSuccessful, errorMessage);
        event.setTransformerClassName(transformerClass);
        event.setOutputType(outputType);

    }
    public void addServiceComponentCallEvent(LocalDateTime startTime, LocalDateTime endTime, String serviceComponentCode,
                                             String serviceProviderCode, boolean isSuccessful, String errorMessage) {

        ServiceComponentCallEvent event = (ServiceComponentCallEvent) addEvent(EventType.SERVICE_COMPONENT_CALL, startTime, endTime, isSuccessful, errorMessage);
        event.setServiceComponentCode(serviceComponentCode);
        event.setServiceProviderCode(serviceProviderCode);

    }
    public Event addEvent(EventType type, LocalDateTime startTime, LocalDateTime endTime, boolean isSuccessful, String errorMessage) {
        if (null == events)
            events = new ArrayList<>();
        Event event = EventFactory.createNewEvent(type, startTime, endTime, errorMessage, isSuccessful);
        events.add(event);
        return event;
    }
}

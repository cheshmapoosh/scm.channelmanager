package ir.daneshrefah.scm.common.model.message;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.model.service.Service;

import java.io.Serializable;
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
public class Message implements Serializable {
    private Header header;
    private Status status;
    private List<Error> errors;
    private List<Event> events;
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

    public void addError(Error error, Status status) {
        if (null == errors)
            errors = new ArrayList<>();
        errors.add(error);
        setStatus(status);
    }

    public void addTransformEvent(LocalDateTime startTime, LocalDateTime endTime, String transformerClass,
                                  boolean isSuccessful, Object error, Object input, Object output, String outputType,
                                  String invokerClassName) {

        TransformEvent event = (TransformEvent) addEvent(EventType.TRANSFORM, startTime, endTime, isSuccessful, error,
                input, output);
        event.setTransformerClassName(transformerClass);
        event.setOutputType(outputType);
        event.setInvokerClassName(invokerClassName);

    }
    public void addServiceCallEvent(LocalDateTime startTime, LocalDateTime endTime, Service service, boolean isSuccessful,
                                    Object error, Object input, Object output) {

        ServiceCallEvent event = (ServiceCallEvent) addEvent(EventType.SERVICE_CALL, startTime, endTime, isSuccessful,
                error, input, output);
        event.setServiceCode(service.getCode());
        event.setImplementationType(service.getImplementationType());
        event.setAdditionalInfo(service.getServiceInfo());

    }
    public Event addEvent(EventType type, LocalDateTime startTime, LocalDateTime endTime, boolean isSuccessful,
                          Object error, Object input, Object output) {
        if (null == events)
            events = new ArrayList<>();
        Event event = EventFactory.createNewEvent(type, startTime, endTime, error, input, output, isSuccessful);
        events.add(event);
        return event;
    }

    public void addEvents(List<Event> events) {
        if (null == events)
            return;

        if (null == this.events)
            this.events = new ArrayList<>();
        this.events.addAll(events);
    }

    public void addErrors(List<Error> errors) {
        if (null == errors)
            return;

        if (null == this.errors)
            this.errors = new ArrayList<>();
        this.errors.addAll(errors);
    }

}

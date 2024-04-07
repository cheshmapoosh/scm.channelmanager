package ir.daneshrefah.scm.plugin.api.transformer;


import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.exception.BaseException;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.logging.api.EventProducer;
import ir.daneshrefah.scm.logging.domain.event.Event;
import ir.daneshrefah.scm.logging.domain.event.EventType;
import ir.daneshrefah.scm.plugin.api.exception.TransformException;

import java.time.Duration;
import java.time.Instant;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-23
 */
public abstract class AbstractTransformer {

    /**
     * Creates a new value from message
     *
     * @param  message message
     * @param  metadata metadata for mapping between source and target, refer to ServiceComponentRelation.metadata
     * @return     the transformed value
     */
    public JsonNode transform(Object payload, Message message, JsonNode metadata) {
        Instant startTime = Instant.now();
        JsonNode result = null;
        boolean isSuccessful = true;
        Exception error = null;
        try {
            result = internalTransform(payload, message, metadata);
        } catch (Exception e) {
            isSuccessful = false;
            error = e;
            if (e instanceof BaseException) {
                throw e;
            } else {
                throw new TransformException(this, e);
            }
        } finally {
            String invokerClassName = Thread.currentThread().getStackTrace()[2].getClassName();
            logTransformEvent(message, payload, result, startTime, error);
//            message.addTransformEvent(startTime, endTime, this.getClass().getName(), isSuccessful, error, payload, result,
//                    (null != result ? result.getClass().getName() : "null"), invokerClassName);
        }
        return result;
    }

    protected abstract JsonNode internalTransform(Object payload, Message message, JsonNode metadata);

    private void logTransformEvent(Message message, Object input, Object output, Instant startTime, Exception error) {
        Instant endTime = Instant.now();
        /*Event event = Event.builder()
                .type(EventType.TRANSFORM)
                .status(message.getStatus())
                .correlationId(message.getHeader().getCorrelationId())
                .source(null)
                .terminalCode(message.getHeader().getServiceAccess().getTerminal().getCode())
                .channelCode(message.getHeader().getChannel().getCode())
                .startTime(startTime)
                .endTime(endTime)
                .durationMillis(Duration.between(startTime, endTime).toMillis())
                .threadName(Thread.currentThread().getName())
                .input(input)
                .output(output)
                .error(error)
                .sourceClassName(this.getClass().getSimpleName())
                .build();*/
//        EventProducer.getInstance().sendEvent(event);
    }
}

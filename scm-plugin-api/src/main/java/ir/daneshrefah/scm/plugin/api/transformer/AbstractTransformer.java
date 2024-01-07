package ir.daneshrefah.scm.plugin.api.transformer;


import ir.daneshrefah.scm.common.exception.BaseException;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.logging.api.EventProducer;
import ir.daneshrefah.scm.logging.domain.event.Event;
import ir.daneshrefah.scm.logging.domain.event.TransformEvent;
import ir.daneshrefah.scm.plugin.api.exception.TransformException;
import ir.daneshrefah.scm.utils.ClassUtils;

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
    public Object transform(Object payload, Message message, String metadata) {
        Instant startTime = Instant.now();
        Object result = null;
        boolean isSuccessful = true;
        Exception error = null;
        try {
            result = internalTransform(payload, message, metadata);
        } catch (Exception e) {
            isSuccessful = false;
            error = ClassUtils.cloneExceptionWithoutStackTrace(e);
            if (e instanceof BaseException) {
                throw e;
            } else {
                throw new TransformException(this);
            }
        } finally {
            String invokerClassName = Thread.currentThread().getStackTrace()[2].getClassName();
            logTransformEvent(message, startTime, error);
//            message.addTransformEvent(startTime, endTime, this.getClass().getName(), isSuccessful, error, payload, result,
//                    (null != result ? result.getClass().getName() : "null"), invokerClassName);
        }
        return result;
    }

    public abstract Object internalTransform(Object payload, Message message, String metadata);

    private void logTransformEvent(Message message, Instant startTime, Exception error) {
        Instant endTime = Instant.now();
        Event event = TransformEvent.builder()
                .correlationId(message.getHeader().getCorrelationId())
                .clientCorrelationId(message.getHeader().getClientCorrelationId())
                .startTimestamp(startTime)
//                .input(input)
                .error(error)
                .threadName(Thread.currentThread().getName())
                .sourceClassName(this.getClass().getSimpleName())
                .clientAgent(message.getHeader().getClientAgent())
                .serverHost(message.getHeader().getServerHost())
                .terminalCode(message.getHeader().getTerminalCode())
                .endTimestamp(endTime)
                .durationMillis(Duration.between(startTime, endTime).toMillis())
                .output(message)
                .build();
        EventProducer.getInstance().sendEvent(event);
    }
}

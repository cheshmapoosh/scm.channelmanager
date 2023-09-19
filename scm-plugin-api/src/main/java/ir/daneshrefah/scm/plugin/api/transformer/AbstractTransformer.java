package ir.daneshrefah.scm.plugin.api.transformer;


import ir.daneshrefah.scm.plugin.api.exception.BaseException;
import ir.daneshrefah.scm.plugin.api.exception.TransformException;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.plugin.api.utils.ClassLoader;

import java.time.LocalDateTime;

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
        LocalDateTime startTime = LocalDateTime.now();
        Object result = null;
        boolean isSuccessful = true;
        Exception error = null;
        try {
            result = internalTransform(payload, message, metadata);
        } catch (Exception e) {
            isSuccessful = false;
            error = ClassLoader.cloneExceptionWithoutStackTrace(e);
            if (e instanceof BaseException) {
                throw e;
            } else {
                throw new TransformException(this);
            }
        } finally {
            LocalDateTime endTime = LocalDateTime.now();
            String invokerClassName = Thread.currentThread().getStackTrace()[2].getClassName();
            message.addTransformEvent(startTime, endTime, this.getClass().getName(), isSuccessful, error, payload, result,
                    (null != result ? result.getClass().getName() : "null"), invokerClassName);
        }
        return result;
    }
    public abstract Object internalTransform(Object payload, Message message, String metadata);

}

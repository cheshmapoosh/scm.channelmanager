package ir.daneshrefah.scm.plugin.api.transformer;


import ir.daneshrefah.scm.plugin.api.model.message.EventType;
import ir.daneshrefah.scm.plugin.api.model.message.Message;

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
     * @param  inputSchema source JsonSchema
     * @param  outputSchema target JsonSchema
     * @param  message message
     * @param  metadata metadata for mapping between source and target, refer to ServiceComponentRelation.metadata
     * @return     the transformed value
     */
    public Object transform(Message message, String metadata) {
        LocalDateTime startTime = LocalDateTime.now();
        Object result = null;
        boolean isSuccessful = true;
        String errorMessage = null;
        try {
            result = internalTransform(message, metadata);
        } catch (Exception e) {
            isSuccessful = false;
            errorMessage = e.getMessage();
            throw e;
        } finally {
            LocalDateTime endTime = LocalDateTime.now();
            message.addTransformEvent(startTime, endTime, this.getClass().getName(), isSuccessful, errorMessage,
                    (null != result ? result.getClass().getName() : "null"));
        }
        return result;
    }
    public abstract Object internalTransform(Message message, String metadata);

}

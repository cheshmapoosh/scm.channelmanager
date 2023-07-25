package ir.daneshrefah.scm.plugin.api.transformer;


import ir.daneshrefah.scm.plugin.api.model.message.Message;

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
    public abstract Object transform(Object inputSchema, Object outputSchema, Message message, String metadata);

}

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
    public abstract Object transform(Object inputSchema, Object outputSchema, Message message);

}

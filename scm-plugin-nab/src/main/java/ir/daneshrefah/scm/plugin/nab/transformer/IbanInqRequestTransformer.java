package ir.daneshrefah.scm.plugin.nab.transformer;

import ir.daneshrefah.scm.plugin.api.model.message.Message;
import ir.daneshrefah.scm.plugin.api.transformer.AbstractTransformer;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-24
 */
public class IbanInqRequestTransformer extends AbstractTransformer {

    @Override
    public Object transform(Object inputSchema, Object outputSchema, Message message, String metadata) {
        return "{test: test}";
    }

}

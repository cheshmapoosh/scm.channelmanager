package ir.daneshrefah.scm.connector.nab.transformer;

import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.AbstractTransformer;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-23
 */
public class IbanInqResponseTransformer extends AbstractTransformer {

    @Override
    public Object transform(Object inputSchema, Object outputSchema, Message message) {
        return "test4";
    }

}

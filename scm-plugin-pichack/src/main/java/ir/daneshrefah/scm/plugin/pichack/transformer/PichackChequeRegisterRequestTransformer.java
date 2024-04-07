package ir.daneshrefah.scm.plugin.pichack.transformer;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.model.message.Message;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-04-07
 */
public class PichackChequeRegisterRequestTransformer extends AbstractPichackRequestTransformer {

    @Override
    protected JsonNode internalTransform(Object payload, Message message, JsonNode metadata) {
        return null;
    }

}

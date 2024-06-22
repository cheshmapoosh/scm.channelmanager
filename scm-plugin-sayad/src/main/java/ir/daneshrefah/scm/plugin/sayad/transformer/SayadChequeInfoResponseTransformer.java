package ir.daneshrefah.scm.plugin.sayad.transformer;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.plugin.api.transformer.AbstractJsonTransformer;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-04-08
 */
public class SayadChequeInfoResponseTransformer extends AbstractJsonTransformer {

    @Override
    protected JsonNode internalTransform(Object payload, Message message, JsonNode metadata) {
        return null;
    }
}

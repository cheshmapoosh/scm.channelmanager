package ir.daneshrefah.scm.core.transformer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.plugin.api.transformer.AbstractTransformer;
import org.springframework.stereotype.Service;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-09
 */
@Service
public class NullTransformer extends AbstractTransformer {

    @Override
    public JsonNode internalTransform(Object payload, Message message, String metadata) {
        return NullNode.getInstance();
    }

}

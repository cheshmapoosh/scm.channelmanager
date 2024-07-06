package ir.daneshrefah.scm.plugin.api.transformer;

import ir.daneshrefah.scm.common.model.message.Message;

import java.util.Map;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-04-07
 */
public class ServiceCodeLookupTransformer extends DynamicLookupTransformer {

    public ServiceCodeLookupTransformer(Map<String, AbstractJsonTransformer> transformerMap) {
        super(transformerMap);
    }

    protected String extractTransformerKey(Message message) {
        return message.getHeader().getService().getCode();
    }
    
}

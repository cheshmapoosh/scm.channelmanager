package ir.daneshrefah.scm.plugin.api.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import ir.daneshrefah.scm.common.budle.ResourceBundleMessageKey;
import ir.daneshrefah.scm.common.constant.BundleParameterPattern;
import ir.daneshrefah.scm.common.data.service.bundle.CacheableResourceBundleService;
import ir.daneshrefah.scm.common.exception.ServiceInvalidMetadataException;
import ir.daneshrefah.scm.common.model.message.Message;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-03-27
 */
public class MessageConverterDictionary extends ConverterDictionary<Message> {

    public MessageConverterDictionary(Message source) {
        super(source);
    }

    @Override
    public JsonNode convert(ParameterDefinition definition) {
        String[] fromValue = definition.getFromValue().split("\\.");
        JsonNode resultNode = getSource().getPayload();
        for (int i = 0; i < fromValue.length; i++) {
            String propertyName = fromValue[i];
            if (resultNode.isObject()) {
                resultNode = resultNode.get(propertyName);
            } else {
                if (definition.isMandatory()) {
                    String serviceCode = getSource().getHeader().getServiceAccess().getService().getCode();
                    throw new ServiceInvalidMetadataException(serviceCode, definition.getFromValue());
                }
                resultNode = JsonNodeFactory.instance.nullNode();
                break;
            }
        }
        return resultNode;
    }

}

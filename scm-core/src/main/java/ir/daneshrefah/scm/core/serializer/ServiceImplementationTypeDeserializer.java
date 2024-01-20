package ir.daneshrefah.scm.core.serializer;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.model.service.ServiceImplementationType;

import java.io.IOException;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-20
 */
public class ServiceImplementationTypeDeserializer extends JsonDeserializer<ServiceImplementationType> {

    public static final ServiceImplementationTypeDeserializer INSTANT = new ServiceImplementationTypeDeserializer();

    @Override
    public ServiceImplementationType deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException, JacksonException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        if (null == node || node.isNull())
            return null;
        if (node.isNumber())
            return ServiceImplementationType.findByCode(node.asInt());
        return ServiceImplementationType.valueOf(node.asText());
    }

}

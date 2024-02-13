package ir.daneshrefah.scm.core.serializer;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.model.person.PersonType;

import java.io.IOException;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-28
 */
public class PersonTypeDeserializer extends JsonDeserializer<PersonType> {

    public static final PersonTypeDeserializer INSTANT = new PersonTypeDeserializer();

    @Override
    public PersonType deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException, JacksonException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        if (null == node || node.isNull())
            return null;
        PersonType result = null;
        if (node.isNumber()) {
            result = PersonType.findByCode(node.asInt());
        }
        if (null == result) {
            result = PersonType.valueOf(node.asText());
        }
        return result;
    }

}

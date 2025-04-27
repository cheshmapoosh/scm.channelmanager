package ir.daneshrefah.scm.core.integration.template;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import ir.daneshrefah.scm.common.model.message.Message;
import org.apache.commons.text.StringSubstitutor;

import java.util.Map;

public class PlaceholderServiceMetadataTemplate extends AbstractServiceMetadataTemplate {
    private final String template;
    private final String name;
    private final ObjectReader reader;

    public PlaceholderServiceMetadataTemplate(String name, String template, ObjectMapper mapper) {
        this.template = template;
        this.name = name;
        this.reader = mapper.readerFor(new TypeReference<Map<String, Object>>() {});
    }

    @Override
    public String render(Message message) {
        StringSubstitutor substitutor = new StringSubstitutor(createContext(message));
        return substitutor.replace(template);
    }

    @Override
    public JsonNode renderAsJson(Message message) {
        String render = this.render(message);
        try {
            return reader.readTree(render);
        } catch (JsonProcessingException e) {
            //            TODO SCMNEW-4: throw scm exception
            throw new RuntimeException(e);
        }

    }

    @Override
    public Map<String, Object> renderAsMap(Message message) {
        String rendered = this.render(message);
        try {
            return reader.readValue(rendered);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}

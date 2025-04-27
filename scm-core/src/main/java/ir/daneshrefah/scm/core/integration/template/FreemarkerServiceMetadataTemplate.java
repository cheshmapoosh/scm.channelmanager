package ir.daneshrefah.scm.core.integration.template;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import ir.daneshrefah.scm.common.model.message.Message;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

public class FreemarkerServiceMetadataTemplate extends AbstractServiceMetadataTemplate {

    private final Template template;
    private final ObjectReader reader;

    public FreemarkerServiceMetadataTemplate(String name, String template, ObjectMapper mapper, Configuration cfg) throws IOException {
        this.template = new Template(name, new StringReader(template), cfg);
        this.reader = mapper.readerFor(new TypeReference<Map<String, Object>>() {
        });
    }

    @Override
    public String render(Message message) {
        try (StringWriter writer = new StringWriter()) {
            template.process(createContext(message), writer);
            return writer.toString();
        } catch (Exception e) {
//            TODO SCMNEW-4: throw scm exception
            throw new RuntimeException("Freemarker render failed", e);
        }
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
        String render = this.render(message);
        try {
            return reader.readValue(render);
        } catch (JsonProcessingException e) {
            //            TODO SCMNEW-4: throw scm exception
            throw new RuntimeException(e);
        }
    }
}

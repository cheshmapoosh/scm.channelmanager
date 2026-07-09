package ir.daneshrefah.scm.observation.starter;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ObservationDocumentSerializer {
    private static final byte[] NEWLINE = new byte[]{'\n'};

    private final ObjectMapper objectMapper;

    public ObservationDocumentSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper.copy()
                .disable(SerializationFeature.INDENT_OUTPUT)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public String serialize(Map<String, Object> document) {
        try {
            return objectMapper.writeValueAsString(document);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize observation document", e);
        }
    }

    public byte[] serializeJsonl(Map<String, Object> document) {
        byte[] json = serialize(document).getBytes(StandardCharsets.UTF_8);
        byte[] line = new byte[json.length + NEWLINE.length];
        System.arraycopy(json, 0, line, 0, json.length);
        System.arraycopy(NEWLINE, 0, line, json.length, NEWLINE.length);
        return line;
    }
}

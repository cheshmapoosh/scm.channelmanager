package ir.daneshrefah.scm.process.service.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.*;
import ir.daneshrefah.scm.process.exception.JsonNodeNullException;
import ir.daneshrefah.scm.process.exception.JsonSchemaNullException;

import java.util.Locale;
import java.util.Set;

public class ValidationSchema {

    private ValidationSchema() {
    }

    public static <T> Set<ValidationMessage> validate(T t, JsonNode jsonSchema) throws JsonProcessingException {
        return validate(getJsonNode(t), jsonSchema, "");
    }

    public static <T> Set<ValidationMessage> validate(T t, JsonNode jsonSchema, String locale) throws JsonProcessingException {
        return validate(getJsonNode(t), jsonSchema, locale);
    }

    public static Set<ValidationMessage> validate(JsonNode inputJson, JsonNode jsonSchema) throws JsonProcessingException {
        return validate(getJsonNode(inputJson), jsonSchema, "");
    }

    private static Set<ValidationMessage> validate(JsonNode inputJson, JsonNode jsonSchema, String local) {
        if (inputJson == null) {
            throw new JsonNodeNullException("ValidationSchema", "JSON node cannot be null.");
        }
        if (jsonSchema == null || jsonSchema.isEmpty()) {
            throw new JsonSchemaNullException("ValidationSchema", "JSON schema cannot be null or empty.");
        }
        JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);
        JsonSchema schema = schemaFactory.getSchema(jsonSchema);
        return schema.validate(inputJson, (executionContext) -> {
            ExecutionConfig executionConfig = new ExecutionConfig();
            if (local.equalsIgnoreCase("fa-IR")) {   // TODO Constant
                Locale locale = new Locale("fa", "IR");// TODO Constant
                executionConfig.setLocale(locale);
                executionContext.setExecutionConfig(executionConfig);
            }
        });
    }

    public static JsonNode getJsonNode(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(json);
    }

    private static <T> JsonNode getJsonNode(T t) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(t);
        return mapper.readTree(jsonString);
    }
}

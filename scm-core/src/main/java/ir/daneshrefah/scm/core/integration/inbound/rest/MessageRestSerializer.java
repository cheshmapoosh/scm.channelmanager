package ir.daneshrefah.scm.core.integration.inbound.rest;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import ir.daneshrefah.scm.plugin.api.model.message.*;
import ir.daneshrefah.scm.plugin.api.model.message.Error;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Iterator;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-30
 */
public class MessageRestSerializer extends JsonSerializer<Message> {

    @Override
    public void serialize(Message value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("status", value.getStatus().name());
        if (Status.SC_SUCCESS.equals(value.getStatus())) {
            JsonNode payload = value.getPayload();
            if (null != payload && payload.isArray()) {
                gen.writeObjectField("results", payload);
            } else if (null != payload && payload.isObject()) {
                gen.writeObjectField("result", payload);
            }
        } else {
            if (null != value.getErrors()) {
                gen.writeArrayFieldStart("errors");
                for (Iterator<Error> iterator = value.getErrors().iterator(); iterator.hasNext(); ) {
                    Error error = iterator.next();
                    gen.writeStartObject();
                    gen.writeStringField("code", error.getCode());
                    gen.writeStringField("message", error.getMessage());
                    gen.writeStringField("source", error.getSource());
                    gen.writeStringField("sourceErrorCode", error.getSourceErrorCode());
                    gen.writeEndObject();
                }
                gen.writeEndArray();
            }
        }
        gen.writeObjectField("events", value.getEvents());
        gen.writeObjectField("responseTimestamp", LocalDateTime.now());
        gen.writeEndObject();
    }

}

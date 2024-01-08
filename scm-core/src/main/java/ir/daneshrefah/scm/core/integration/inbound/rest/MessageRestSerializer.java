package ir.daneshrefah.scm.core.integration.inbound.rest;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.Status;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;

import java.io.IOException;
import java.time.LocalDateTime;

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
        writeAuthentication(value, gen);
        if (Status.SC_SUCCESS.equals(value.getStatus())) {
            JsonNode payload = value.getPayload();
//            if (null != payload && payload.isArray()) {
                gen.writeObjectField("result", payload);
//            } else if (null != payload && payload.isObject()) {
//                gen.writeObjectField("result", payload);
//            }
        } else {
            /*if (null != value.getErrors()) {
                gen.writeArrayFieldStart("errors");
                for (Iterator<Error> iterator = value.getErrors().iterator(); iterator.hasNext(); ) {
                    Error error = iterator.next();
                    gen.writeStartObject();
                    gen.writeStringField("code", error.getCode());
                    gen.writeStringField("message", error.getMessage());
                    gen.writeObjectField("source", error.getSource());
                    gen.writeStringField("sourceErrorCode", error.getSourceErrorCode());
                    gen.writeEndObject();
                }
                gen.writeEndArray();
            }*/
            gen.writeObjectField("errors", value.getErrors());
        }
//        gen.writeObjectField("events", value.getEvents());
        gen.writeObjectField("responseTimestamp", LocalDateTime.now());
        gen.writeEndObject();
    }

    private void writeAuthentication(Message value, JsonGenerator gen) throws IOException {
        UserAuthentication authentication = (UserAuthentication) value.getHeader().getAuthentication();
        gen.writeFieldName("authentication");
        gen.writeStartObject();
        gen.writeStringField("isAuthenticated", String.valueOf(authentication.isAuthenticated()));
        gen.writeStringField("isAnonymous", String.valueOf(authentication.isAnonymous()));
        gen.writeStringField("hasError", String.valueOf(authentication.hasError()));
        gen.writeStringField("username", authentication.getName()); //TODO username
        gen.writeStringField("sessionId", authentication.getDetails().getSessionId());
        gen.writeStringField("terminal", authentication.getTerminalCode());
        gen.writeEndObject();
    }

}

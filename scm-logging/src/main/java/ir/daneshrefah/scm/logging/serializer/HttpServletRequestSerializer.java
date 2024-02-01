package ir.daneshrefah.scm.logging.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.stream.Collectors;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-31
 */
public class HttpServletRequestSerializer extends JsonSerializer<HttpServletRequest> {

    public static final HttpServletRequestSerializer INSTANT = new HttpServletRequestSerializer();

    @Override
    public void serialize(HttpServletRequest request, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("url", request.getRequestURL().toString());
        gen.writeStringField("method", request.getMethod());
        gen.writeStringField("address", request.getRemoteAddr());
        try {
            gen.writeStringField("body", request.getReader().lines().collect(Collectors.joining()));
        } catch (IOException e) {
            gen.writeStringField("body", "Error reading request body");
        }
        gen.writeEndObject();
    }
}

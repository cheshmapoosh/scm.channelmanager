package ir.daneshrefah.scm.logging.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.camel.Exchange;

import java.io.IOException;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-31
 */
public class ExchangeSerializer extends JsonSerializer<Exchange> {

    public static final ExchangeSerializer INSTANT = new ExchangeSerializer();

    @Override
    public void serialize(Exchange request, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("type", "Exchange");
        gen.writeEndObject();
    }
}

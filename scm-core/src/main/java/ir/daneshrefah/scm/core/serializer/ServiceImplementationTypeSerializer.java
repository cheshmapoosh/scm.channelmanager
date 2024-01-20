package ir.daneshrefah.scm.core.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import ir.daneshrefah.scm.common.model.service.ServiceImplementationType;

import java.io.IOException;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-20
 */
public class ServiceImplementationTypeSerializer extends JsonSerializer<ServiceImplementationType> {

    public static final ServiceImplementationTypeSerializer INSTANT = new ServiceImplementationTypeSerializer();

    @Override
    public void serialize(ServiceImplementationType value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeNumber(value.getCode());
    }
}

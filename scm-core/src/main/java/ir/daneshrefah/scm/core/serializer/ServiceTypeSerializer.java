package ir.daneshrefah.scm.core.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import ir.daneshrefah.scm.common.model.service.ServiceType;

import java.io.IOException;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-20
 */
public class ServiceTypeSerializer extends JsonSerializer<ServiceType> {

    public static final ServiceTypeSerializer INSTANT = new ServiceTypeSerializer();

    @Override
    public void serialize(ServiceType value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeNumber(value.getCode());
    }
}

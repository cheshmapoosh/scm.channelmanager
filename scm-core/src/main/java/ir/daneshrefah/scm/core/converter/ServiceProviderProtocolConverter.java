package ir.daneshrefah.scm.core.converter;

import ir.daneshrefah.scm.common.model.service.ServiceProviderProtocol;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Objects;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-06-05
 */
@Converter
public class ServiceProviderProtocolConverter implements AttributeConverter<ServiceProviderProtocol, Integer> {

    @Override
    public Integer convertToDatabaseColumn(ServiceProviderProtocol protocol) {
        if (Objects.isNull(protocol)) {
            return null;
        }
        return protocol.getCode();
    }

    @Override
    public ServiceProviderProtocol convertToEntityAttribute(Integer code) {
        if (Objects.isNull(code)) {
            return null;
        }
        return ServiceProviderProtocol.findByCode(code);
    }

}
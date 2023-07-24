package ir.daneshrefah.scm.core.converter;

import ir.daneshrefah.scm.plugin.api.model.service.ServiceImplementationType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-22
 */
@Converter
public class ServiceImplementationTypeConverter implements AttributeConverter<ServiceImplementationType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(ServiceImplementationType enumValue) {
        return enumValue.getCode();
    }

    @Override
    public ServiceImplementationType convertToEntityAttribute(Integer code) {
        return ServiceImplementationType.findByCode(code);
    }
}
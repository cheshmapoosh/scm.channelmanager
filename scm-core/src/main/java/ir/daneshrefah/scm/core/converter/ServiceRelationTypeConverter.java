package ir.daneshrefah.scm.core.converter;

import ir.daneshrefah.scm.plugin.api.model.service.composition.ServiceRelationType;
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
public class ServiceRelationTypeConverter implements AttributeConverter<ServiceRelationType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(ServiceRelationType enumValue) {
        return enumValue.getCode();
    }

    @Override
    public ServiceRelationType convertToEntityAttribute(Integer code) {
        return ServiceRelationType.findByCode(code);
    }
}
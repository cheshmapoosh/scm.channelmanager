package ir.daneshrefah.scm.core.converter;

import ir.daneshrefah.scm.plugin.api.model.service.ServiceComponentRelationType;
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
public class ServiceComponentRelationTypeConverter implements AttributeConverter<ServiceComponentRelationType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(ServiceComponentRelationType enumValue) {
        return enumValue.getCode();
    }

    @Override
    public ServiceComponentRelationType convertToEntityAttribute(Integer code) {
        return ServiceComponentRelationType.findByCode(code);
    }
}
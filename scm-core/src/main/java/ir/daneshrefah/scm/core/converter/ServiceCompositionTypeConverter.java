package ir.daneshrefah.scm.core.converter;

import ir.daneshrefah.scm.common.model.service.ServiceCompositionType;
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
public class ServiceCompositionTypeConverter implements AttributeConverter<ServiceCompositionType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(ServiceCompositionType enumValue) {
        return enumValue.getCode();
    }

    @Override
    public ServiceCompositionType convertToEntityAttribute(Integer code) {
        return ServiceCompositionType.findByCode(code);
    }

}
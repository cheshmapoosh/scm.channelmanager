package ir.daneshrefah.scm.repository.converter;

import ir.daneshrefah.scm.common.model.service.ServiceType;
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
public class ServiceTypeConverter implements AttributeConverter<ServiceType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(ServiceType enumValue) {
        return enumValue.getCode();
    }

    @Override
    public ServiceType convertToEntityAttribute(Integer code) {
        return ServiceType.findByCode(code);
    }
}
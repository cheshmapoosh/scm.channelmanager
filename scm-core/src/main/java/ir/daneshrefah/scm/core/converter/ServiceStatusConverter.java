package ir.daneshrefah.scm.core.converter;

import ir.daneshrefah.scm.common.model.service.ServiceStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-20
 */
@Converter
public class ServiceStatusConverter implements AttributeConverter<ServiceStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(ServiceStatus enumValue) {
        if (null == enumValue)
            return null;
        return enumValue.getCode();
    }

    @Override
    public ServiceStatus convertToEntityAttribute(Integer code) {
        if (null == code)
            return null;
        return ServiceStatus.findByCode(code);
    }
}
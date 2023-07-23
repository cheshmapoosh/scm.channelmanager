package ir.daneshrefah.scm.repository.converter;

import ir.daneshrefah.scm.common.model.service.ServiceRelation;
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
public class ServiceRelationTypeConverter implements AttributeConverter<ServiceRelation.ServiceRelationType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(ServiceRelation.ServiceRelationType enumValue) {
        return enumValue.getCode();
    }

    @Override
    public ServiceRelation.ServiceRelationType convertToEntityAttribute(Integer code) {
        return ServiceRelation.ServiceRelationType.findByCode(code);
    }
}
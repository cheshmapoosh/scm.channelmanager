package ir.daneshrefah.scm.core.converter;

import ir.daneshrefah.scm.common.model.service.parameter.ParameterActionType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ParameterActionTypeConverter implements AttributeConverter<ParameterActionType,Integer> {
    @Override
    public Integer convertToDatabaseColumn(ParameterActionType parameterActionType) {
        return parameterActionType.getCode();
    }

    @Override
    public ParameterActionType convertToEntityAttribute(Integer integer) {
        return ParameterActionType.findByCode(integer);
    }
}

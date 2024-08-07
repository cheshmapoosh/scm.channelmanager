package ir.daneshrefah.scm.core.converter;

import ir.daneshrefah.scm.common.model.service.parameter.ParameterDatasourceProperty;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ParameterDatasourcePropertyConverter implements AttributeConverter<ParameterDatasourceProperty, Integer> {
    @Override
    public Integer convertToDatabaseColumn(ParameterDatasourceProperty parameterDatasourceEntity) {
        return parameterDatasourceEntity.getCode();
    }

    @Override
    public ParameterDatasourceProperty convertToEntityAttribute(Integer integer) {
        return ParameterDatasourceProperty.findByCode(integer);
    }
}

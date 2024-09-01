package ir.daneshrefah.scm.core.converter;

import ir.daneshrefah.scm.common.model.service.parameter.DatasourceConditionOperation;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class DatasourceConditionOperationConverter implements AttributeConverter<DatasourceConditionOperation, Integer> {
    @Override
    public Integer convertToDatabaseColumn(DatasourceConditionOperation parameterConditionOperation) {
        return parameterConditionOperation.getCode();
    }

    @Override
    public DatasourceConditionOperation convertToEntityAttribute(Integer s) {
        return DatasourceConditionOperation.findByCode(s);
    }
}

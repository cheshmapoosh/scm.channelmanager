package ir.daneshrefah.scm.common.data.entity.operation;

import ir.daneshrefah.scm.common.model.operation.OperationType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class OperationTypeConverter
        implements AttributeConverter<OperationType, String> {

    @Override
    public String convertToDatabaseColumn(OperationType attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public OperationType convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }

        try {
            return OperationType.valueOf(dbData);
        } catch (IllegalArgumentException ex) {
            return OperationType.UNKNOWN;
        }
    }
}
package ir.daneshrefah.scm.core.converter;

import ir.daneshrefah.scm.common.type.ConditionType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-13
 */
@Converter
public class ConditionTypeConverter implements AttributeConverter<ConditionType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(ConditionType enumValue) {
        return enumValue.getCode();
    }

    @Override
    public ConditionType convertToEntityAttribute(Integer code) {
        return ConditionType.findByCode(code);
    }
}
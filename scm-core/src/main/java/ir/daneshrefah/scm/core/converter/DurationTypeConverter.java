package ir.daneshrefah.scm.core.converter;

import ir.daneshrefah.scm.common.type.DurationType;
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
public class DurationTypeConverter implements AttributeConverter<DurationType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(DurationType enumValue) {
        return enumValue.getCode();
    }

    @Override
    public DurationType convertToEntityAttribute(Integer code) {
        return DurationType.findByCode(code);
    }
}
package ir.daneshrefah.scm.core.converter;

import ir.daneshrefah.scm.common.type.PeriodType;
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
public class PeriodTypeConverter implements AttributeConverter<PeriodType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(PeriodType enumValue) {
        return enumValue.getCode();
    }

    @Override
    public PeriodType convertToEntityAttribute(Integer code) {
        return PeriodType.findByCode(code);
    }
}
package ir.daneshrefah.scm.core.converter;

import ir.daneshrefah.scm.common.model.service.TransformerType;
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
public class TransformerTypeConverter implements AttributeConverter<TransformerType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(TransformerType enumValue) {
        return enumValue.getCode();
    }

    @Override
    public TransformerType convertToEntityAttribute(Integer code) {
        return TransformerType.findByCode(code);
    }
}
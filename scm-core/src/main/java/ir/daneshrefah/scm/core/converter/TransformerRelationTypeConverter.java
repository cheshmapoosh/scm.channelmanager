package ir.daneshrefah.scm.core.converter;

import ir.daneshrefah.scm.common.model.transformer.TransformerRelationType;
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
public class TransformerRelationTypeConverter implements AttributeConverter<TransformerRelationType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(TransformerRelationType enumValue) {
        return enumValue.getCode();
    }

    @Override
    public TransformerRelationType convertToEntityAttribute(Integer code) {
        return TransformerRelationType.findByCode(code);
    }
}
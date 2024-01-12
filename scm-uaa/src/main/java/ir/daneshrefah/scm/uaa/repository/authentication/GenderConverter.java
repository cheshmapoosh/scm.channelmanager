package ir.daneshrefah.scm.uaa.repository.authentication;

import ir.daneshrefah.scm.uaa.common.type.Gender;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-11
 */
@Converter
public class GenderConverter implements AttributeConverter<Gender, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Gender attribute) {
        return attribute.getCode();
    }

    @Override
    public Gender convertToEntityAttribute(Integer dbData) {
        return Gender.findByCode(dbData);
    }
}

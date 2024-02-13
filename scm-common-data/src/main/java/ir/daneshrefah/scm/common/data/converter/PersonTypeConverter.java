package ir.daneshrefah.scm.common.data.converter;

import ir.daneshrefah.scm.common.model.person.PersonType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-13
 */
@Converter
public class PersonTypeConverter implements AttributeConverter<PersonType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(PersonType attribute) {
        return attribute.getCode();
    }

    @Override
    public PersonType convertToEntityAttribute(Integer dbData) {
        if (null == dbData)
            return null;
        return PersonType.findByCode(dbData);
    }
}

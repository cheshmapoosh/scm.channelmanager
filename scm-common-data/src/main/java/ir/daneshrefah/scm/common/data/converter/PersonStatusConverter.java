package ir.daneshrefah.scm.common.data.converter;

import ir.daneshrefah.scm.common.model.person.PersonStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-06-16
 */
@Converter
public class PersonStatusConverter implements AttributeConverter<PersonStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(PersonStatus attribute) {
        return attribute.getCode();
    }

    @Override
    public PersonStatus convertToEntityAttribute(Integer dbData) {
        if (null == dbData)
            return null;
        return PersonStatus.findByCode(dbData);
    }
}

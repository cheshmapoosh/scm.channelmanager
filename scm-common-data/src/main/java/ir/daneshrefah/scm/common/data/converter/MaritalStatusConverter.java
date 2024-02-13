package ir.daneshrefah.scm.common.data.converter;

import ir.daneshrefah.scm.common.model.person.MaritalStatus;
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
public class MaritalStatusConverter implements AttributeConverter<MaritalStatus, String> {

    @Override
    public String convertToDatabaseColumn(MaritalStatus attribute) {
        return attribute.getCode();
    }

    @Override
    public MaritalStatus convertToEntityAttribute(String dbData) {
        return MaritalStatus.findByCode(dbData);
    }
}

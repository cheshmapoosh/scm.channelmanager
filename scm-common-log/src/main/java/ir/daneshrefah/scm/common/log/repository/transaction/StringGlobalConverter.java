package ir.daneshrefah.scm.common.log.repository.transaction;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-11
 */
@Converter(autoApply = true)
public class StringGlobalConverter implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return attribute;
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (null == dbData)
            return null;
        return dbData.trim();
    }

}

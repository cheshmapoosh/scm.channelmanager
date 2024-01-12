package ir.daneshrefah.scm.uaa.repository.authentication;

import ir.daneshrefah.scm.uaa.common.type.Nationality;
import ir.daneshrefah.scm.utils.string.StringUtils;
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
public class NationalityConverter implements AttributeConverter<Nationality, String> {

    @Override
    public String convertToDatabaseColumn(Nationality attribute) {
        return attribute.getCode();
    }

    @Override
    public Nationality convertToEntityAttribute(String dbData) {
        if (StringUtils.isEmpty(dbData))
            return null;
        return Nationality.findByCode(dbData.trim());
    }
}

package ir.daneshrefah.scm.core.converter;

import ir.daneshrefah.scm.common.model.authority.AuthorityType;
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
public class AuthorityTypeConverter implements AttributeConverter<AuthorityType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(AuthorityType enumValue) {
        return enumValue.getCode();
    }

    @Override
    public AuthorityType convertToEntityAttribute(Integer code) {
        return AuthorityType.findByCode(code);
    }
}
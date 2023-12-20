package ir.daneshrefah.scm.core.converter;

import ir.daneshrefah.scm.uaa.common.type.AuthenticationMethod;
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
public class AuthenticationMethodConverter implements AttributeConverter<AuthenticationMethod, String> {

    @Override
    public String convertToDatabaseColumn(AuthenticationMethod enumValue) {
        return enumValue.getCode();
    }

    @Override
    public AuthenticationMethod convertToEntityAttribute(String code) {
        return AuthenticationMethod.findByCode(code);
    }
}
package ir.daneshrefah.scm.uaa.repository.authentication;

import ir.daneshrefah.scm.uaa.common.type.AuthenticationMethod;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-09
 */
@Converter
public class AuthenticationMethodConverter implements AttributeConverter<AuthenticationMethod, Integer> {

    @Override
    public Integer convertToDatabaseColumn(AuthenticationMethod attribute) {
        return attribute.getDbRef();
    }

    @Override
    public AuthenticationMethod convertToEntityAttribute(Integer dbData) {
        return AuthenticationMethod.findByDbRef(dbData);
    }
}

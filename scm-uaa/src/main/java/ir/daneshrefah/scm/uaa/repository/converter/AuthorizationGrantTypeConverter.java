package ir.daneshrefah.scm.uaa.repository.converter;

import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.domain.client.ClientVersionStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-13
 */
@Converter
public class AuthorizationGrantTypeConverter implements AttributeConverter<AuthorizationGrantType, String> {

    @Override
    public String convertToDatabaseColumn(AuthorizationGrantType enumValue) {
        if (null == enumValue)
            return null;
        return enumValue.getCode();
    }

    @Override
    public AuthorizationGrantType convertToEntityAttribute(String code) {
        if (null == code)
            return null;
        return AuthorizationGrantType.findByCode(code);
    }
}
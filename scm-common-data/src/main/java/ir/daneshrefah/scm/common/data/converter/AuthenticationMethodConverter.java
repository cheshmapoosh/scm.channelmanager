package ir.daneshrefah.scm.common.data.converter;

import ir.daneshrefah.scm.common.model.user.AuthenticationMethod;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class AuthenticationMethodConverter implements AttributeConverter<AuthenticationMethod, Integer> {

    @Override
    public Integer convertToDatabaseColumn(AuthenticationMethod enumValue) {
        return enumValue.getDbRef();
    }

    @Override
    public AuthenticationMethod convertToEntityAttribute(Integer code) {
        return AuthenticationMethod.findByDbRef(code);
    }
}
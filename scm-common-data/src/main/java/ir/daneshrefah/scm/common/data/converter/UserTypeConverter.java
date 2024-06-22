package ir.daneshrefah.scm.common.data.converter;

import ir.daneshrefah.scm.common.model.user.UserType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-06-18
 */
@Converter
public class UserTypeConverter implements AttributeConverter<UserType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(UserType attribute) {
        return attribute.getCode();
    }

    @Override
    public UserType convertToEntityAttribute(Integer dbData) {
        if (null == dbData)
            return null;
        return UserType.findByCode(dbData);
    }
}

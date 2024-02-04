package ir.daneshrefah.scm.uaa.converter;

import ir.daneshrefah.scm.uaa.domain.client.ClientVersionStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-04
 */
@Converter
public class ClientVersionStatusConverter implements AttributeConverter<ClientVersionStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(ClientVersionStatus enumValue) {
        if (null == enumValue)
            return null;
        return enumValue.getCode();
    }

    @Override
    public ClientVersionStatus convertToEntityAttribute(Integer code) {
        if (null == code)
            return null;
        return ClientVersionStatus.findByCode(code);
    }
}
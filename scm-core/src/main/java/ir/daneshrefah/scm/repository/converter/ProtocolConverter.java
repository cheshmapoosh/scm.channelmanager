package ir.daneshrefah.scm.repository.converter;

import ir.daneshrefah.scm.common.model.service.ServiceType;
import ir.daneshrefah.scm.common.model.terminal.Protocol;
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
public class ProtocolConverter implements AttributeConverter<Protocol, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Protocol enumValue) {
        return enumValue.getCode();
    }

    @Override
    public Protocol convertToEntityAttribute(Integer code) {
        return Protocol.findByCode(code);
    }
}
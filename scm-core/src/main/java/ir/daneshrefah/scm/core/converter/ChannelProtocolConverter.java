package ir.daneshrefah.scm.core.converter;

import ir.daneshrefah.scm.common.model.terminal.ChannelProtocol;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-17
 */
@Converter
public class ChannelProtocolConverter implements AttributeConverter<ChannelProtocol, Integer> {

    @Override
    public Integer convertToDatabaseColumn(ChannelProtocol enumValue) {
        return enumValue.getCode();
    }

    @Override
    public ChannelProtocol convertToEntityAttribute(Integer code) {
        return ChannelProtocol.findByCode(code);
    }
}
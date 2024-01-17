package ir.daneshrefah.scm.common.data.converter;

import ir.daneshrefah.scm.common.model.terminal.TerminalStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-01-17
 */
@Converter
public class TerminalStatusConverter implements AttributeConverter<TerminalStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(TerminalStatus enumValue) {
        return enumValue.getCode();
    }

    @Override
    public TerminalStatus convertToEntityAttribute(Integer code) {
        return TerminalStatus.findByCode(code);
    }
}
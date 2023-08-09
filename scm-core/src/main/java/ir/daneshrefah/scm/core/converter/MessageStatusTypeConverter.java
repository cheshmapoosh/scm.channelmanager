package ir.daneshrefah.scm.core.converter;

import ir.daneshrefah.scm.plugin.api.model.message.Status;
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
public class MessageStatusTypeConverter implements AttributeConverter<Status, String> {

    @Override
    public String convertToDatabaseColumn(Status enumValue) {
        return enumValue.getCode();
    }

    @Override
    public Status convertToEntityAttribute(String code) {
        return Status.findByCode(code);
    }
}
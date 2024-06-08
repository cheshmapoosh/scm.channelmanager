package ir.daneshrefah.scm.common.data.converter;

import ir.daneshrefah.scm.common.model.message.MessageStatus;
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
public class MessageStatusTypeConverter implements AttributeConverter<MessageStatus, String> {

    @Override
    public String convertToDatabaseColumn(MessageStatus enumValue) {
        return enumValue.getCode();
    }

    @Override
    public MessageStatus convertToEntityAttribute(String code) {
        return MessageStatus.findByCode(code);
    }
}
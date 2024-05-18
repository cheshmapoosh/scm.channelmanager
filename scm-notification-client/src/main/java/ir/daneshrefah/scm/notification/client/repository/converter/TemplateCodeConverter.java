package ir.daneshrefah.scm.notification.client.repository.converter;

import ir.daneshrefah.scm.common.model.notification.constants.NotificationTemplate;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class TemplateCodeConverter implements AttributeConverter<NotificationTemplate,String> {
    @Override
    public String convertToDatabaseColumn(NotificationTemplate templateCode) {
        return templateCode.getValue();
    }

    @Override
    public NotificationTemplate convertToEntityAttribute(String code) {
        return NotificationTemplate.findByCode(code);
    }
}

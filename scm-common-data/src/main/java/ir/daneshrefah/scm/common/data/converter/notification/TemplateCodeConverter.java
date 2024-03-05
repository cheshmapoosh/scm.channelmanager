package ir.daneshrefah.scm.common.data.converter.notification;

import ir.daneshrefah.scm.common.model.notification.constants.TemplateCode;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class TemplateCodeConverter implements AttributeConverter<TemplateCode,String> {
    @Override
    public String convertToDatabaseColumn(TemplateCode templateCode) {
        return templateCode.getValue();
    }

    @Override
    public TemplateCode convertToEntityAttribute(String code) {
        return TemplateCode.findByCode(code);
    }
}

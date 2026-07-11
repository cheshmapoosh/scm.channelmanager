package ir.daneshrefah.scm.core.entity.gateway;



import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinitionType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class ChannelServiceDefinitionTypeConverter
        implements AttributeConverter<ChannelServiceDefinitionType, String> {

    @Override
    public String convertToDatabaseColumn(ChannelServiceDefinitionType attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public ChannelServiceDefinitionType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }

        try {
            return ChannelServiceDefinitionType.valueOf(dbData.trim());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
package ir.daneshrefah.scm.common.data.converter;

import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class RoutingStrategyConverter implements AttributeConverter<RoutingStrategy, String> {

    @Override
    public String convertToDatabaseColumn(RoutingStrategy attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public RoutingStrategy convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }

        try {
            return RoutingStrategy.valueOf(dbData);
        } catch (IllegalArgumentException e) {

            return null;
        }
    }
}

package ir.daneshrefah.scm.core.converter;

import ir.daneshrefah.scm.common.model.service.ExternalServiceBodyType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Objects;

@Converter
public class ExternalServiceRequestBodyTypeConverter implements AttributeConverter<ExternalServiceBodyType,String > {

    @Override
    public String convertToDatabaseColumn(ExternalServiceBodyType externalServiceRequestBodyType) {
        if (Objects.nonNull(externalServiceRequestBodyType)){
            return externalServiceRequestBodyType.name();
        }
        return null;
    }

    @Override
    public ExternalServiceBodyType convertToEntityAttribute(String string) {
        return ExternalServiceBodyType.find(string);
    }
}

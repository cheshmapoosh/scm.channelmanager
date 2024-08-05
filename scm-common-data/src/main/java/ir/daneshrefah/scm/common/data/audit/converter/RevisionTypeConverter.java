package ir.daneshrefah.scm.common.data.audit.converter;

import ir.daneshrefah.scm.common.data.audit.model.constants.RevisionType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class RevisionTypeConverter implements AttributeConverter<RevisionType,Integer> {
    @Override
    public Integer convertToDatabaseColumn(RevisionType revisionType) {
        return revisionType.getStatus();
    }

    @Override
    public RevisionType convertToEntityAttribute(Integer integer) {
        return RevisionType.findByStatus(integer).orElse(null);
    }
}

package ir.daneshrefah.scm.task.converter;

import ir.daneshrefah.scm.task.constant.DefinitionTypeEnum;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class DefinitionTypeConvertor implements AttributeConverter<DefinitionTypeEnum, Integer> {

    @Override
    public Integer convertToDatabaseColumn(DefinitionTypeEnum definitionTypeEnum) {
        return definitionTypeEnum.getCode();
    }

    @Override
    public DefinitionTypeEnum convertToEntityAttribute(Integer code) {
        return DefinitionTypeEnum.findByCode(code);
    }
}

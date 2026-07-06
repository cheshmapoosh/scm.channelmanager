package ir.daneshrefah.scm.task.converter;

import ir.daneshrefah.scm.task.constant.ExecutionMethodTypeEnum;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ExecutionMethodConvertor implements AttributeConverter<ExecutionMethodTypeEnum, Integer> {

    @Override
    public Integer convertToDatabaseColumn(ExecutionMethodTypeEnum executionMethodTypeEnum) {
        return executionMethodTypeEnum.getCode();
    }

    @Override
    public ExecutionMethodTypeEnum convertToEntityAttribute(Integer code) {
        return ExecutionMethodTypeEnum.findByCode(code);
    }
}

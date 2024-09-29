package ir.daneshrefah.scm.task.converter;

import ir.daneshrefah.scm.task.constant.ProcessCodeEnum;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ProcessCodeConverter implements AttributeConverter<ProcessCodeEnum, Integer> {

    @Override
    public Integer convertToDatabaseColumn(ProcessCodeEnum processCodeEnum) {
        return processCodeEnum.getCode();
    }

    @Override
    public ProcessCodeEnum convertToEntityAttribute(Integer code) {
        return ProcessCodeEnum.findByCode(code);
    }
}

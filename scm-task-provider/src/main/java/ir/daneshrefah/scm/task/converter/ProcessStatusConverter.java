package ir.daneshrefah.scm.task.converter;

import ir.daneshrefah.scm.task.constant.ProcessStatusEnum;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ProcessStatusConverter implements AttributeConverter<ProcessStatusEnum,Integer> {

    @Override
    public Integer convertToDatabaseColumn(ProcessStatusEnum processStatusEnum) {
        return processStatusEnum.getStatusCode();
    }

    @Override
    public ProcessStatusEnum convertToEntityAttribute(Integer code) {
        return ProcessStatusEnum.findByStatusCode(code);
    }
}

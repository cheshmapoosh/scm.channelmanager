package ir.daneshrefah.scm.task.converter;

import ir.daneshrefah.scm.task.constant.ProcessWatcherEnum;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ProcessWatcherTypeConverter implements AttributeConverter<ProcessWatcherEnum, Integer> {

    @Override
    public Integer convertToDatabaseColumn(ProcessWatcherEnum processWatcherEnum) {
        return processWatcherEnum.getCode();
    }

    @Override
    public ProcessWatcherEnum convertToEntityAttribute(Integer code) {
        return ProcessWatcherEnum.findByCode(code);
    }
}

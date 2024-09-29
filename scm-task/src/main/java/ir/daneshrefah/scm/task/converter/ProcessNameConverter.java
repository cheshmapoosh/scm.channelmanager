package ir.daneshrefah.scm.task.converter;

import ir.daneshrefah.scm.task.constant.ProcessNameEnum;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ProcessNameConverter implements AttributeConverter<ProcessNameEnum,String> {

    @Override
    public String convertToDatabaseColumn(ProcessNameEnum processName) {
        return processName.name();
    }

    @Override
    public ProcessNameEnum convertToEntityAttribute(String str) {
        return ProcessNameEnum.valueOf(str);
    }
}

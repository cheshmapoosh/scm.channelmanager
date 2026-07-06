package ir.daneshrefah.scm.provider.task.converter;

import ir.daneshrefah.scm.provider.task.constant.ProcessNameEnum;
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

package ir.daneshrefah.scm.provider.task.converter;

import ir.daneshrefah.scm.provider.task.constant.TaskStatusEnum;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class TaskStatusConverter implements AttributeConverter<TaskStatusEnum, Integer> {

    @Override
    public Integer convertToDatabaseColumn(TaskStatusEnum taskStatusEnum) {
        return taskStatusEnum.getStatusCode();
    }

    @Override
    public TaskStatusEnum convertToEntityAttribute(Integer code) {
        return TaskStatusEnum.findByStatusCode(code);
    }
}

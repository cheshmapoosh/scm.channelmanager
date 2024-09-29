package ir.daneshrefah.scm.task.mapper;

import ir.daneshrefah.scm.common.constant.AccessibleLocale;
import ir.daneshrefah.scm.common.data.service.bundle.ResourceBundleService;
import ir.daneshrefah.scm.task.constant.TaskStatusEnum;
import ir.daneshrefah.scm.task.entity.TaskEntity;
import ir.daneshrefah.scm.task.model.TaskResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Mapper(componentModel = "spring")
public abstract class TaskMapper {

    @Autowired
    private ResourceBundleService bundle;

    @Autowired
    private ProcessInstanceMapper processInstanceMapper;

    @Mapping(source = "createAt", target = "createAt", qualifiedByName = "mapDateToString")
    @Mapping(source = "taskStatus", target = "statusName", qualifiedByName = "mapTaskStatusName")
    @Mapping(source = "updateAt", target = "updateAt", qualifiedByName = "mapDateToString")
    @Mapping(target = "processInstance", ignore = true)
    public abstract TaskResponse toTaskResponse(TaskEntity taskEntity);


    public List<TaskResponse> toTaskResponseList(List<TaskEntity> taskEntities) {
        return taskEntities.stream().map(this::toTaskResponse).toList();
    }

    public List<TaskResponse> toTaskResponseListWithProcessInstance(List<TaskEntity> taskEntities) {
        List<TaskResponse> taskResponseList = new ArrayList<>();
        for (TaskEntity task : taskEntities) {
            TaskResponse taskResponse = toTaskResponse(task);
            taskResponse.setProcessInstance(processInstanceMapper.toProcessInstanceResponse(task.getProcessInstance()));
            taskResponseList.add(taskResponse);
        }
        return taskResponseList;
    }

    public TaskResponse toTaskResponseWithProcessInstance(TaskEntity taskEntity) {
        TaskResponse taskResponse = toTaskResponse(taskEntity);
        taskResponse.setProcessInstance(processInstanceMapper.toProcessInstanceResponse(taskEntity.getProcessInstance()));
        return taskResponse;
    }

    @Named("mapDateToString")
    String mapCreateAt(Date createAt) {
        return createAt != null ? String.valueOf(createAt.getTime()) : null;
    }

    @Named("mapTaskStatusName")
    String mapStatusName(TaskStatusEnum taskStatusEnum) {
        return bundle.get(AccessibleLocale.FA_IR.getLocale(), taskStatusEnum.name()).orElse(taskStatusEnum.name());
    }
}

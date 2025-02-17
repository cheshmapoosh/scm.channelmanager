package ir.daneshrefah.scm.task.mapper;

import ir.daneshrefah.scm.common.constant.AccessibleLocale;
import ir.daneshrefah.scm.common.data.service.bundle.ResourceBundleService;
import ir.daneshrefah.scm.task.constant.ProcessStatusEnum;
import ir.daneshrefah.scm.task.entity.ProcessInstanceEntity;
import ir.daneshrefah.scm.task.model.ProcessInstanceApproveResponse;
import ir.daneshrefah.scm.task.model.ProcessInstanceResponse;
import ir.daneshrefah.scm.task.model.ProcessInstanceStartResponse;
import ir.daneshrefah.scm.task.model.ProcessInstanceUpdateResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

@Mapper(componentModel = "spring")
public abstract class ProcessInstanceMapper {

    @Autowired
    private ResourceBundleService bundle;


    @Mapping(target = "tasks",ignore = true)
    @Mapping(source = "createAt", target = "createAt", qualifiedByName = "mapCreateAt")
    @Mapping(source = "processStatus", target = "statusName", qualifiedByName = "mapProcessStatusName")
    public abstract ProcessInstanceResponse toProcessInstanceResponse(ProcessInstanceEntity processInstance);

    @Mapping(target = "tasks",ignore = true)
    @Mapping(source = "createAt", target = "createAt", qualifiedByName = "mapCreateAt")
    @Mapping(source = "processStatus", target = "statusName", qualifiedByName = "mapProcessStatusName")
    public abstract ProcessInstanceApproveResponse toProcessInstanceApproveResponse(ProcessInstanceEntity processInstance);

    @Mapping(source = "createAt", target = "createAt", qualifiedByName = "mapCreateAt")
    @Mapping(source = "processStatus", target = "statusName", qualifiedByName = "mapProcessStatusName")
    public abstract ProcessInstanceStartResponse toProcessInstanceStartResponse(ProcessInstanceEntity processInstance);


    public abstract ProcessInstanceUpdateResponse toProcessInstanceUpdateResponse(ProcessInstanceEntity processInstance);

    @Named("mapCreateAt")
     String mapCreateAt(Date createAt) {
        return createAt != null ? String.valueOf(createAt.getTime()) : null;
    }

    @Named("mapProcessStatusName")
    String mapStatusName(ProcessStatusEnum processStatusEnum) {
        return bundle.get(AccessibleLocale.FA_IR.getLocale(), processStatusEnum.name()).orElse(processStatusEnum.name());
    }


}

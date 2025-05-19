package ir.daneshrefah.scm.task.mapper;

import ir.daneshrefah.scm.common.constant.AccessibleLocale;
import ir.daneshrefah.scm.common.data.service.bundle.ResourceBundleService;
import ir.daneshrefah.scm.common.data.service.person.PersonService;
import ir.daneshrefah.scm.common.model.person.GeneralLegalPerson;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.common.model.person.GeneralRealPerson;
import ir.daneshrefah.scm.task.constant.ProcessStatusEnum;
import ir.daneshrefah.scm.task.constant.TaskStatusEnum;
import ir.daneshrefah.scm.task.entity.ProcessInstanceEntity;
import ir.daneshrefah.scm.task.entity.TaskEntity;
import ir.daneshrefah.scm.task.model.IssuerModel;
import ir.daneshrefah.scm.task.model.ProcessInstanceResponse;
import ir.daneshrefah.scm.task.model.TaskResponse;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
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

    @Autowired
    private PersonService personService;


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
        Integer loggedInUserId = AuthenticationUtils.getLoggedInUserId();
        for (TaskEntity task : taskEntities) {
            TaskResponse taskResponse = toTaskResponse(task);
            ProcessInstanceResponse processInstanceResponse = processInstanceMapper.toProcessInstanceResponse(task.getProcessInstance());
            if (!(processInstanceResponse.getProcessStatus().equals(ProcessStatusEnum.COMPLETE) || processInstanceResponse.getProcessStatus().equals(ProcessStatusEnum.CANCEL)) && allowCancelProcess(task.getProcessInstance(), loggedInUserId)) {
                processInstanceResponse.setCanCancel(true);
            }
            IssuerModel issuerModel = mapToUserModel(task.getProcessInstance());
            processInstanceResponse.setCreatedBy(issuerModel);
            taskResponse.setProcessInstance(processInstanceResponse);
            taskResponseList.add(taskResponse);
        }
        return taskResponseList;
    }

    private IssuerModel mapToUserModel(ProcessInstanceEntity processInstance) {
        GeneralPerson person = personService.findPersonByPersonId(processInstance.getCreateBy());
        IssuerModel issuerModel = new IssuerModel();
        if (person instanceof GeneralRealPerson realPerson) {
            issuerModel.setNationalId(realPerson.getNationalCode());
            issuerModel.setFirstName(realPerson.getFirstName());
            issuerModel.setLastName(realPerson.getLastName());
        } else if (person instanceof GeneralLegalPerson legalPerson) {
            issuerModel.setNationalId(legalPerson.getNationalId());
            issuerModel.setFirstName(legalPerson.getTitle());
            issuerModel.setLastName(legalPerson.getTitleEnglish());
        }
        issuerModel.setPersonType(person.getPersonType());
        return issuerModel;
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

    boolean allowCancelProcess(ProcessInstanceEntity processInstance, Integer loggedInUserId) {
        return processInstance.getConfirmUserId() != null && processInstance.getConfirmUserId().equals(loggedInUserId);
    }
}

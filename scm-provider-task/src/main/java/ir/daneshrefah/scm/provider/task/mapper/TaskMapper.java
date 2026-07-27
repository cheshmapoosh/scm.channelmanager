package ir.daneshrefah.scm.provider.task.mapper;

import ir.daneshrefah.scm.common.constant.AccessibleLocale;
import ir.daneshrefah.scm.common.data.service.bundle.ResourceBundleService;
import ir.daneshrefah.scm.common.data.service.person.PersonService;
import ir.daneshrefah.scm.common.model.person.GeneralLegalPerson;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.common.model.person.GeneralRealPerson;
import ir.daneshrefah.scm.provider.task.constant.ProcessStatusEnum;
import ir.daneshrefah.scm.provider.task.constant.TaskStatusEnum;
import ir.daneshrefah.scm.provider.task.entity.ProcessInstanceEntity;
import ir.daneshrefah.scm.provider.task.entity.TaskEntity;
import ir.daneshrefah.scm.provider.task.model.IssuerModel;
import ir.daneshrefah.scm.provider.task.model.ProcessInstanceResponse;
import ir.daneshrefah.scm.provider.task.model.TaskResponse;
import ir.daneshrefah.scm.provider.task.workflow.TaskWorkflowRecoveryStore;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public abstract class TaskMapper {

    @Autowired
    private ResourceBundleService bundle;

    @Autowired
    private ProcessInstanceMapper processInstanceMapper;

    @Autowired
    private PersonService personService;

    @Autowired
    private TaskWorkflowRecoveryStore taskWorkflowRecoveryStore;

    @Mapping(source = "createAt", target = "createAt", qualifiedByName = "mapDateToString")
    @Mapping(source = "taskStatus", target = "statusName", qualifiedByName = "mapTaskStatusName")
    @Mapping(source = "updateAt", target = "updateAt", qualifiedByName = "mapDateToString")
    @Mapping(target = "processInstance", ignore = true)
    public abstract TaskResponse toTaskResponse(TaskEntity taskEntity);


    public List<TaskResponse> toTaskResponseList(List<TaskEntity> taskEntities) {
        Map<Long, String> executionIds = executionIds(taskEntities);
        List<TaskResponse> taskResponseList = new ArrayList<>();
        for (TaskEntity taskEntity : taskEntities) {
            TaskResponse taskResponse = toTaskResponse(taskEntity);
            taskResponse.setProcessInstance(processResponse(
                    taskEntity.getProcessInstance(),
                    executionIds
            ));
            taskResponseList.add(taskResponse);
        }
        return taskResponseList;
    }

    public List<TaskResponse> toTaskResponseListWithProcessInstance(List<TaskEntity> taskEntities) {
        Map<Long, String> executionIds = executionIds(taskEntities);
        List<TaskResponse> taskResponseList = new ArrayList<>();
        Integer loggedInUserId = AuthenticationUtils.getLoggedInUserId();
        for (TaskEntity task : taskEntities) {
            TaskResponse taskResponse = toTaskResponse(task);
            ProcessInstanceResponse processInstanceResponse = processResponse(
                    task.getProcessInstance(),
                    executionIds
            );
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
        Map<Long, String> executionIds = executionIds(List.of(taskEntity));
        taskResponse.setProcessInstance(processResponse(
                taskEntity.getProcessInstance(),
                executionIds
        ));
        return taskResponse;
    }

    private Map<Long, String> executionIds(List<TaskEntity> tasks) {
        return taskWorkflowRecoveryStore.findExecutionIdsByProcessIds(
                tasks.stream()
                        .map(TaskEntity::getProcessInstance)
                        .map(ProcessInstanceEntity::getId)
                        .distinct()
                        .toList()
        );
    }

    private ProcessInstanceResponse processResponse(
            ProcessInstanceEntity process,
            Map<Long, String> executionIds
    ) {
        ProcessInstanceResponse response =
                processInstanceMapper.toProcessInstanceResponse(process);
        response.setExecutionId(executionIds.get(process.getId()));
        return response;
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

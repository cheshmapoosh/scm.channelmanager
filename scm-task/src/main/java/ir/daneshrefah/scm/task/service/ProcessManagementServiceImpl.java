package ir.daneshrefah.scm.task.service;

import ir.daneshrefah.scm.common.constant.AccessibleLocale;
import ir.daneshrefah.scm.common.data.service.bundle.ResourceBundleService;
import ir.daneshrefah.scm.common.data.service.person.PersonService;
import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.common.exception.AccessDeniedException;
import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.model.message.MessageInput;
import ir.daneshrefah.scm.common.model.person.GeneralLegalPerson;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.common.model.person.GeneralRealPerson;
import ir.daneshrefah.scm.task.constant.ProcessStatusEnum;
import ir.daneshrefah.scm.task.constant.TaskStatusEnum;
import ir.daneshrefah.scm.task.entity.ProcessInstanceEntity;
import ir.daneshrefah.scm.task.entity.TaskEntity;
import ir.daneshrefah.scm.task.entity.TaskLogEntity;
import ir.daneshrefah.scm.task.exception.InvalidProcessStatusException;
import ir.daneshrefah.scm.task.exception.InvalidTaskStatusException;
import ir.daneshrefah.scm.task.exception.ProcessAuthorityException;
import ir.daneshrefah.scm.task.exception.ProcessInstanceCompleteException;
import ir.daneshrefah.scm.task.mapper.ProcessInstanceMapper;
import ir.daneshrefah.scm.task.model.*;
import ir.daneshrefah.scm.task.repository.ProcessInstanceRepository;
import ir.daneshrefah.scm.task.repository.ProcessInstanceSpecs;
import ir.daneshrefah.scm.task.utils.PageableUtils;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.utils.MessageInputContext;
import ir.daneshrefah.scm.utils.string.ArchiveUtils;
import ir.daneshrefah.scm.utils.string.StringUtils;
import ir.daneshrefah.scm.utils.validation.ChainValidation;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_ACCESS_DENIED;

@Service
@AllArgsConstructor
public class ProcessManagementServiceImpl implements ProcessManagementService {

    private final ProcessInstanceWatcherService processInstanceWatcherService;
    private final ProcessTaskDefinitionService processTaskDefinitionService;
    private final ProcessInstanceRepository processInstanceRepository;
    private final ProcessInstanceMapper processInstanceMapper;
    private final TaskLogService taskLogService;
    private final ResourceBundleService bundle;
    private final PersonService personService;
    private final TaskAssetService taskAssetService;

    @Override
    public ProcessInstanceStartResponse start(ProcessInstanceStartRequest request) {
        processTaskDefinitionService.validateProcessBeforeStart(request);
        ProcessInstanceEntity processInstanceEntity = createProcessInstanceEntity(request);
        processInstanceEntity.getTasks().forEach(taskEntity -> taskEntity.setProcessInstance(processInstanceEntity));
        ProcessInstanceEntity processInstance = processInstanceRepository.save(processInstanceEntity);
        return processInstanceMapper.toProcessInstanceStartResponse(processInstance);
    }

    private ProcessInstanceEntity createProcessInstanceEntity(ProcessInstanceStartRequest request) {
        UserModel confirmUserModel = request.getConfirmUser();
        GeneralPerson generalPerson = null;
        ProcessInstanceEntity processInstanceEntity = new ProcessInstanceEntity();
        processInstanceEntity.setAccountNo(request.getAccountNo());
        processInstanceEntity.setTransactionData(request.getTransactionData());
        processInstanceEntity.setProcessCode(request.getProcessCode());
        processInstanceEntity.setAmount(request.getAmount());
        processInstanceEntity.setDescription(request.getDescription());
        processInstanceEntity.setDestination(request.getDestination());
        if (Objects.nonNull(request.getConfirmUser()) && StringUtils.isNotBlank(request.getConfirmUser().getNationalId())) {
            generalPerson = findUserByPersonTypeAndNationalCodeAndSubOrg(confirmUserModel);
            processInstanceEntity.setConfirmUserId(generalPerson.getId());
        }
        processInstanceEntity.setArchiveNo(ArchiveUtils.calculateOneMonthArchiveNo());
        processInstanceEntity.setProcessStatus(ProcessStatusEnum.PENDING);
        processInstanceEntity.setCreateBy(AuthenticationUtils.getLoggedInUserId());
        processInstanceEntity.setCreateAt(new Date());
        processInstanceEntity.setWatcherEntities(List.of(processInstanceWatcherService.createProcessInstanceWatcherEntity(processInstanceEntity, ProcessStatusEnum.START)));
        processInstanceEntity.addTaskEntities(createTaskEntity(request));
        createGlobalTaskIfNeeded(processInstanceEntity, generalPerson);
        return processInstanceEntity;
    }

    private void createGlobalTaskIfNeeded(ProcessInstanceEntity processInstance, GeneralPerson person) {
        boolean shouldCreateGlobalTask = processInstance.getConfirmUserId() != null && processInstance.getTasks().stream().noneMatch(task -> task.getUserId().equals(processInstance.getConfirmUserId()));
        if (shouldCreateGlobalTask) {
            TaskEntity globalTask = new TaskEntity();
            globalTask.setUserId(processInstance.getConfirmUserId());
            if (person instanceof GeneralRealPerson realPerson) {
                globalTask.setFullName(realPerson.getFirstName() + " " + realPerson.getLastName());
            }
            globalTask.setTaskStatus(TaskStatusEnum.PENDING);
            globalTask.setGlobal(true);
            globalTask.setArchiveNo(ArchiveUtils.calculateOneMonthArchiveNo());
            globalTask.setCreatedBy(AuthenticationUtils.getLoggedInUserId());
            globalTask.setCreateAt(new Date());
            processInstance.addTaskEntity(globalTask);
        }
    }

    private List<TaskEntity> createTaskEntity(ProcessInstanceStartRequest processInstanceStartRequest) {
        List<TaskEntity> taskEntities = new ArrayList<>();
        for (UserModel userModel : processInstanceStartRequest.getUsers()) {
            TaskEntity taskEntity = new TaskEntity();
            GeneralPerson findUser = findUserByPersonTypeAndNationalCodeAndSubOrg(userModel);
            taskEntity.setUserId(findUser.getId());
            if (findUser instanceof GeneralRealPerson person) {
                taskEntity.setFullName(person.getFirstName() + " " + person.getLastName());
            }
            taskEntity.setTaskStatus(TaskStatusEnum.PENDING);
            taskEntity.setArchiveNo(ArchiveUtils.calculateOneMonthArchiveNo());
            taskEntity.setCreatedBy(AuthenticationUtils.getLoggedInUserId());
            taskEntity.setCreateAt(new Date());
            taskEntities.add(taskEntity);
        }
        return taskEntities;
    }

    private ProcessInstanceResponse mapToProcessInstanceResponse(ProcessInstanceEntity processInstance) {
        ProcessInstanceResponse processInstanceResponse = new ProcessInstanceResponse();
        processInstanceResponse.setId(processInstance.getId());
        processInstanceResponse.setAccountNo(processInstance.getAccountNo());
        processInstanceResponse.setProcessStatus(processInstance.getProcessStatus());
        processInstanceResponse.setDescription(processInstance.getDescription());
        processInstanceResponse.setProcessCode(processInstance.getProcessCode());
        processInstanceResponse.setTransactionData(processInstance.getTransactionData());
        processInstanceResponse.setDestination(processInstance.getDestination());
        processInstanceResponse.setAmount(processInstance.getAmount());
        processInstanceResponse.setCreateAt(String.valueOf(processInstance.getCreateAt().getTime()));
        processInstanceResponse.setStatusName(bundle.get(AccessibleLocale.FA_IR.getLocale(), processInstance.getProcessStatus().name()).orElse(processInstance.getProcessStatus().name()));
        List<TaskResponse> taskResponseList = new ArrayList<>();
        for (TaskEntity task : processInstance.getTasks()) {
            TaskResponse taskResponse = new TaskResponse();
            taskResponse.setId(task.getId());
            taskResponse.setFullName(task.getFullName());
            taskResponse.setTaskStatus(task.getTaskStatus());
            taskResponse.setGlobal(task.isGlobal());
            taskResponse.setStatusName(bundle.get(AccessibleLocale.FA_IR.getLocale(), task.getTaskStatus().name()).orElse(task.getTaskStatus().name()));
            taskResponseList.add(taskResponse);
        }
        processInstanceResponse.setTasks(taskResponseList);
        return processInstanceResponse;
    }

    @Override
    public PagedResponseData<ProcessInstanceResponse> findAll(ProcessInstanceFilterRequest request) {
        request = Objects.nonNull(request) ? request : new ProcessInstanceFilterRequest();
        Integer loggedInUserId = AuthenticationUtils.getLoggedInUserId();
        if (request.isReport()) {
            request.setUserId(loggedInUserId);
        } else {
            request.setConfirmUserId(loggedInUserId);
        }
        Pageable pageable = PageableUtils.getPageable(request);
        Page<ProcessInstanceEntity> entities = processInstanceRepository.findAll(ProcessInstanceSpecs.toSpecification(request), pageable);
        return new PagedResponseData<>(request.getPageNo(), request.getPageSize(), entities.getTotalElements(), entities.stream().map(this::mapToProcessInstanceResponse).toList());
    }

    private GeneralPerson findUserByPersonTypeAndNationalCodeAndSubOrg(UserModel confirmUserModel) {
        return personService.findPerson(confirmUserModel.getPersonType(), confirmUserModel.getNationalId(), confirmUserModel.getSubOrganization())
                .orElseThrow(() -> new NoMatchRecordFoundException("user"));
    }

    public ProcessInstanceEntity findByID(Long processId) {
        return processInstanceRepository.findById(processId).orElseThrow(() -> new NoMatchRecordFoundException("processID"));
    }

    public ProcessInstanceUpdateResponse updateDescription(ProcessInstanceUpdateRequest request) {
        ChainValidation.crateValidator(request.getDescription(), "description").checkBlank();
        ProcessInstanceEntity processInstanceEntity = processInstanceRepository.findById(request.getId()).orElseThrow(() -> new NoMatchRecordFoundException("processId"));

        if (!hasUserAccess(processInstanceEntity)) {
            throw new ProcessAuthorityException("update Description", "have not permission");
        }
        if (processInstanceEntity.getProcessStatus().equals(ProcessStatusEnum.COMPLETE)) {
            throw new ProcessInstanceCompleteException("process state", "Cannot update description of a completed process instance.");
        }
        processInstanceEntity.setDescription(request.getDescription());
        processInstanceRepository.save(processInstanceEntity);
        return processInstanceMapper.toProcessInstanceUpdateResponse(processInstanceEntity);
    }

    public boolean hasUserAccess(ProcessInstanceEntity processInstanceEntity) {
        Integer confirmUserId = processInstanceEntity.getConfirmUserId();
        Integer loggedInUser = AuthenticationUtils.getLoggedInUserId();
        if (Objects.equals(confirmUserId, loggedInUser)) {
            return true;
        }
        return processInstanceEntity.getTasks().stream()
                .anyMatch(taskEntity -> Objects.equals(taskEntity.getUserId(), loggedInUser));
    }

    public ProcessInstanceApproveResponse approve(ProcessInstanceApproveRequest request) {
        validateApproveRequest(request);
        ProcessInstanceEntity processInstance = findByID(request.getId());
        Integer loggedInUserId = AuthenticationUtils.getLoggedInUserId();
        validateProcessStatus(processInstance.getProcessStatus(), EnumSet.of(ProcessStatusEnum.WAITING_FOR_CONFIRM));
        validateTaskStates(processInstance, TaskStatusEnum.WAITING_FOR_CONFIRM);
        validateUserAccess(processInstance, loggedInUserId);

        TaskEntity taskEntity = findTaskByStatus(processInstance, TaskStatusEnum.WAITING_FOR_CONFIRM);

        taskEntity.setTaskStatus(TaskStatusEnum.WAITING_FOR_ACKNOWLEDGE);
        processInstance.setProcessStatus(ProcessStatusEnum.WAITING_FOR_ACKNOWLEDGE);
        processInstance.setCorrelationId(request.getCorrelationId());
        ProcessInstanceApproveResponse response = createResponseWithConfirmUser(processInstance);
        List<UserModel> users = getTaskUsers(processInstance);
        response.setUsers(users);

        persistTaskLogEntity(taskEntity);
        processInstanceRepository.save(processInstance);
        return response;
    }

    private ProcessInstanceApproveResponse createResponseWithConfirmUser(ProcessInstanceEntity processInstance) {
        ProcessInstanceApproveResponse response = processInstanceMapper.toProcessInstanceApproveResponse(processInstance);
        if (Objects.nonNull(processInstance.getConfirmUserId())) {
            GeneralPerson confirmUser = personService.findPersonByPersonId(processInstance.getConfirmUserId());
            UserModel userModel = createUserModel(confirmUser);
            response.setConfirmUser(userModel);
        }
        return response;
    }

    private List<UserModel> getTaskUsers(ProcessInstanceEntity processInstance) {
        return processInstance.getTasks().stream()
                .map(task -> {
                    GeneralPerson person = personService.findPersonByPersonId(task.getUserId());
                    UserModel userModel = createUserModel(person);
                    userModel.setSigner(true);
                    return userModel;
                })
                .toList();
    }

    private UserModel createUserModel(GeneralPerson person) {
        UserModel userModel = new UserModel();
        if (person instanceof GeneralRealPerson realPerson) {
            userModel.setNationalId(realPerson.getNationalCode());
        } else if (person instanceof GeneralLegalPerson legalPerson) {
            userModel.setNationalId(legalPerson.getNationalId());
        }
        userModel.setPersonType(person.getPersonType());
        userModel.setCustomerNo(taskAssetService.findCustomerNo(person.getId())
                .orElseThrow(() -> new NoMatchRecordFoundException("customerNo")));
        return userModel;
    }

    private TaskEntity findTaskByStatus(ProcessInstanceEntity processInstance, TaskStatusEnum status) {
        return processInstance.getTasks().stream()
                .filter(task -> task.getTaskStatus().equals(status))
                .findFirst()
                .orElseThrow(() -> new NoMatchRecordFoundException("task"));
    }

    private void validateApproveRequest(ProcessInstanceApproveRequest request) {
        processTaskDefinitionService.validateProcessBeforeApprove(request);
        ValidationUtils.checkEmptyString(request.getCorrelationId(), () -> {
            throw new InvalidInputException("correlationId");
        });
    }

    private void persistTaskLogEntity(TaskEntity taskEntity) {
        TaskLogEntity taskLogEntity = new TaskLogEntity();
        MessageInput context = MessageInputContext.getCurrentContext();
        taskLogEntity.setLastChannelCode(context.getChannel().getCode());
        taskLogEntity.setTaskEntity(taskEntity);
        taskLogEntity.setStatus(TaskStatusEnum.WAITING_FOR_ACKNOWLEDGE);
        taskLogEntity.setArchiveNo(ArchiveUtils.calculateOneMonthArchiveNo());
        taskLogEntity.setCreatedBy(AuthenticationUtils.getLoggedInUserId());
        taskLogEntity.setCreateAt(new Date());
        taskLogService.save(taskLogEntity);
    }

    public void complete(ProcessInstanceCompleteRequest request) {
        validateProcessStatus(request.getStatus(), EnumSet.of(ProcessStatusEnum.COMPLETE, ProcessStatusEnum.FAIL));

        ProcessInstanceEntity processInstance = findByID(request.getId());
        Integer loggedInUserId = AuthenticationUtils.getLoggedInUserId();
        validateProcessStatus(processInstance.getProcessStatus(), EnumSet.of(ProcessStatusEnum.WAITING_FOR_ACKNOWLEDGE));
        validateTaskStates(processInstance, TaskStatusEnum.WAITING_FOR_ACKNOWLEDGE);
        validateUserAccess(processInstance, loggedInUserId);

        MessageInput context = MessageInputContext.getCurrentContext();
        if (ProcessStatusEnum.COMPLETE.equals(request.getStatus())) {
            updateProcessInstanceForCompletion(processInstance, loggedInUserId, context, ProcessStatusEnum.COMPLETE);
            completeTasks(processInstance, TaskStatusEnum.COMPLETE);

            processInstanceRepository.save(processInstance);
        } else {
            updateProcessInstanceForCompletion(processInstance, loggedInUserId, context, ProcessStatusEnum.FAIL);
            completeTasks(processInstance, TaskStatusEnum.CANCEL);
        }
    }

    private void validateProcessStatus(ProcessStatusEnum processStatusEnum, EnumSet<ProcessStatusEnum> enumSet) {
        if (!enumSet.contains(processStatusEnum)) {
            throw new InvalidProcessStatusException("status", "Invalid process status.");
        }
    }

    private void validateTaskStates(ProcessInstanceEntity processInstance, TaskStatusEnum taskStatusEnum) {
        //all task status must be complete or taskStatusEnum except throws exception
        boolean isStateInvalid = processInstance.getTasks().stream().anyMatch(task -> !EnumSet.of(TaskStatusEnum.COMPLETE, taskStatusEnum).contains(task.getTaskStatus()));
        if (isStateInvalid) {
            throw new InvalidTaskStatusException("status", "Invalid task status.");
        }
    }

    private void validateUserAccess(ProcessInstanceEntity processInstance, Integer loggedInUserId) {
        if (processInstance.getConfirmUserId() != null) {
            boolean hasAccess = processInstance.getConfirmUserId().equals(loggedInUserId) || processInstance.getTasks().stream().anyMatch(task -> task.getUserId().equals(loggedInUserId));
            if (!hasAccess) {
                throw new AccessDeniedException("access user", ERROR_CODE_ACCESS_DENIED, "User does not have access to the process.");
            }
        }
    }

    private void updateProcessInstanceForCompletion(ProcessInstanceEntity processInstance, Integer loggedInUserId, MessageInput context, ProcessStatusEnum processStatusEnum) {
        processInstance.setUpdateAt(new Date());
        processInstance.setUpdateBy(loggedInUserId);
        processInstance.setProcessStatus(processStatusEnum);
        processInstance.setLastMessageSequenceId(context.getClientCorrelationId());
    }

    private void completeTasks(ProcessInstanceEntity processInstance, TaskStatusEnum taskStatusEnum) {
        processInstance.getTasks().stream().
                filter(task -> TaskStatusEnum.WAITING_FOR_ACKNOWLEDGE.equals(task.getTaskStatus()))
                .forEach(task -> {
                    task.setUpdateAt(new Date());
                    task.setTaskStatus(taskStatusEnum);
                    taskLogService.mapToTaskLogAndPersist(task);
                });
    }
}


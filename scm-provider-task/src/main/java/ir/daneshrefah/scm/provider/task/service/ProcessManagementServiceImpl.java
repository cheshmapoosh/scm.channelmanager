package ir.daneshrefah.scm.provider.task.service;

import ir.daneshrefah.scm.common.constant.AccessibleLocale;
import ir.daneshrefah.scm.common.data.service.bundle.ResourceBundleService;
import ir.daneshrefah.scm.common.data.service.person.PersonService;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.exception.AccessDeniedException;
import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.person.GeneralLegalPerson;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.common.model.person.GeneralRealPerson;
import ir.daneshrefah.scm.provider.task.constant.ProcessStatusEnum;
import ir.daneshrefah.scm.provider.task.constant.ProcessWatcherEnum;
import ir.daneshrefah.scm.provider.task.constant.TaskStatusEnum;
import ir.daneshrefah.scm.provider.task.entity.ProcessInstanceEntity;
import ir.daneshrefah.scm.provider.task.entity.ProcessInstanceWatcherEntity;
import ir.daneshrefah.scm.provider.task.entity.TaskEntity;
import ir.daneshrefah.scm.provider.task.entity.TaskLogEntity;
import ir.daneshrefah.scm.provider.task.exception.InvalidProcessStatusException;
import ir.daneshrefah.scm.provider.task.exception.InvalidTaskStatusException;
import ir.daneshrefah.scm.provider.task.exception.ProcessAuthorityException;
import ir.daneshrefah.scm.provider.task.exception.ProcessInstanceCompleteException;
import ir.daneshrefah.scm.provider.task.mapper.ProcessInstanceMapper;
import ir.daneshrefah.scm.provider.task.model.*;
import ir.daneshrefah.scm.provider.task.repository.ProcessInstanceRepository;
import ir.daneshrefah.scm.provider.task.repository.ProcessInstanceSpecs;
import ir.daneshrefah.scm.provider.task.utils.PageableUtils;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.utils.string.ArchiveUtils;
import ir.daneshrefah.scm.utils.string.StringUtils;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import lombok.AllArgsConstructor;
import org.apache.camel.Exchange;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_ACCESS_DENIED;
import static ir.daneshrefah.scm.utils.constant.Constants.SCM_PARAMETER_CLIENT_CORRELATION_ID;

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


    private static boolean allowCancelProcess(ProcessInstanceEntity processInstance, Integer loggedInUserId) {
        return processInstance.getConfirmUserId() != null && processInstance.getConfirmUserId().equals(loggedInUserId);
    }

    @Override
    public ProcessInstanceStartResponse start(Exchange exchange, ProcessInstanceStartRequest request) {
        processTaskDefinitionService.validateProcessBeforeStart(exchange,request);
        ProcessInstanceEntity processInstanceEntity = createProcessInstanceEntity(request);
        ProcessInstanceEntity processInstance = processInstanceRepository.save(processInstanceEntity);
        return processInstanceMapper.toProcessInstanceStartResponse(processInstance);
    }

    private ProcessInstanceEntity createProcessInstanceEntity(ProcessInstanceStartRequest request) {
        UserModel confirmUserModel = request.getConfirmUser();
        GeneralPerson generalPerson = null;
        ProcessInstanceEntity processInstanceEntity = new ProcessInstanceEntity();
        processInstanceEntity.setAccountNo(request.getAccountNo());
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
        processInstanceEntity.setWatcherEntities(processInstanceWatcherService.createProcessInstanceWatcherEntity(processInstanceEntity, request.getTransactionData(), ProcessWatcherEnum.REQUEST));
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
            } else if (person instanceof GeneralLegalPerson legalPerson) {
                globalTask.setFullName(legalPerson.getTitle());
            }
            globalTask.setTaskStatus(TaskStatusEnum.PENDING);
            globalTask.setGlobal(true);
            globalTask.setSigner(false);
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
            } else if (findUser instanceof GeneralLegalPerson legalPerson) {
                taskEntity.setFullName(legalPerson.getTitle());
            }
            taskEntity.setTaskStatus(TaskStatusEnum.PENDING);
            taskEntity.setArchiveNo(ArchiveUtils.calculateOneMonthArchiveNo());
            taskEntity.setCreatedBy(AuthenticationUtils.getLoggedInUserId());
            taskEntity.setCreateAt(new Date());
            taskEntity.setSigner(true);
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
        processInstanceResponse.setAttribute(processInstanceMapper.mapToAttribute(processInstance.getWatcherEntities()));
        processInstanceResponse.setTransactionData(processInstanceMapper.mapToMetadata(processInstance.getWatcherEntities()));
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
        Integer loggedInUserId = AuthenticationUtils.getLoggedInUserId();
        if (!(processInstance.getProcessStatus().equals(ProcessStatusEnum.COMPLETE) || processInstance.getProcessStatus().equals(ProcessStatusEnum.CANCEL)) && allowCancelProcess(processInstance, loggedInUserId)) {
            processInstanceResponse.setCanCancel(true);
        }
        processInstanceResponse.setTasks(taskResponseList);
        return processInstanceResponse;
    }

    @Override
    public PagedResponseData<ProcessInstanceResponse> findAll(Exchange exchange, ProcessInstanceFilterRequest request) {
        request = Objects.nonNull(request) ? request : new ProcessInstanceFilterRequest();
        Integer loggedInUserId = AuthenticationUtils.getLoggedInUserId();
        if (request.isReport()) {
            request.setUserId(loggedInUserId);
        } else {
            request.setConfirmUserId(loggedInUserId);
        }
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageableUtils.getPageable(request, sort);
        Page<ProcessInstanceEntity> entities = processInstanceRepository.findAll(ProcessInstanceSpecs.toSpecification(request), pageable);
        return new PagedResponseData<>(request.getPageNo(), request.getPageSize(), entities.getTotalElements(), entities.stream().map(this::mapToProcessInstanceResponse).toList());
    }

    private GeneralPerson findUserByPersonTypeAndNationalCodeAndSubOrg(UserModel confirmUserModel) {
        return personService.findPerson(confirmUserModel.getPersonType(), confirmUserModel.getNationalId(), confirmUserModel.getSubOrganization()).orElseThrow(() -> new NoMatchRecordFoundException("user"));
    }

    @Override
    public ProcessInstanceEntity findByID(Exchange exchange, Long processId) {
        return processInstanceRepository.findById(processId).orElseThrow(() -> new NoMatchRecordFoundException("processID"));
    }

    @Override
    public ProcessInstanceUpdateResponse updateDescription(Exchange exchange, ProcessInstanceUpdateRequest request) {
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
        return processInstanceEntity.getTasks().stream().anyMatch(taskEntity -> Objects.equals(taskEntity.getUserId(), loggedInUser));
    }

    @Override
    public ProcessInstanceApproveResponse approve(Exchange exchange, ProcessInstanceApproveRequest request) {
        validateApproveRequest(exchange, request);
        ProcessInstanceEntity processInstance = findByID(exchange, request.getId());
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

        persistTaskLogEntity(exchange, taskEntity);
        processInstanceRepository.save(processInstance);
        return response;
    }

    private ProcessInstanceApproveResponse createResponseWithConfirmUser(ProcessInstanceEntity processInstance) {
        ProcessInstanceApproveResponse response = processInstanceMapper.toProcessInstanceApproveResponse(processInstance);
        response.setTransactionData(processInstanceMapper.mapToMetadata(processInstance.getWatcherEntities()));
        response.setAttribute(processInstanceMapper.mapToAttribute(processInstance.getWatcherEntities()));
        if (Objects.nonNull(processInstance.getConfirmUserId())) {
            GeneralPerson confirmUser = personService.findPersonByPersonId(processInstance.getConfirmUserId());
            UserModel userModel = createUserModel(confirmUser);
            response.setConfirmUser(userModel);
        }
        return response;
    }

    private List<UserModel> getTaskUsers(ProcessInstanceEntity processInstance) {
        return processInstance.getTasks().stream().filter(TaskEntity::getSigner).map(task -> {
            GeneralPerson person = personService.findPersonByPersonId(task.getUserId());
            return createUserModel(person);
        }).toList();
    }

    private UserModel createUserModel(GeneralPerson person) {
        UserModel userModel = new UserModel();
        if (person instanceof GeneralRealPerson realPerson) {
            userModel.setNationalId(realPerson.getNationalCode());
        } else if (person instanceof GeneralLegalPerson legalPerson) {
            userModel.setNationalId(legalPerson.getNationalId());
        }
        userModel.setPersonType(person.getPersonType());
        userModel.setCustomerId(taskAssetService.findCustomerNo(person.getId()).orElseThrow(() -> new NoMatchRecordFoundException("customerId")));
        return userModel;
    }

    private TaskEntity findTaskByStatus(ProcessInstanceEntity processInstance, TaskStatusEnum status) {
        return processInstance.getTasks().stream().filter(task -> task.getTaskStatus().equals(status)).findFirst().orElseThrow(() -> new NoMatchRecordFoundException("task"));
    }

    private void validateApproveRequest(Exchange exchange, ProcessInstanceApproveRequest request) {
        processTaskDefinitionService.validateProcessBeforeApprove(exchange, request);
        ValidationUtils.checkEmptyString(request.getCorrelationId(), () -> {
            throw new InvalidInputException("correlationId");
        });
    }

    private void persistTaskLogEntity(Exchange exchange, TaskEntity taskEntity) {
        TaskLogEntity taskLogEntity = new TaskLogEntity();
        //TODO TEMPORARY GET CHANNEL CODE FROM EXCHANGE
//        MessageInput context = MessageInputContext.getCurrentContext();
//        taskLogEntity.setLastChannelCode(context.getChannel().getCode());
        taskLogEntity.setLastChannelCode(exchange.getProperty(Message.CHANNEL_CODE, String.class));
        taskLogEntity.setTaskEntity(taskEntity);
        taskLogEntity.setStatus(TaskStatusEnum.WAITING_FOR_ACKNOWLEDGE);
        taskLogEntity.setArchiveNo(ArchiveUtils.calculateOneMonthArchiveNo());
        taskLogEntity.setCreatedBy(AuthenticationUtils.getLoggedInUserId());
        taskLogEntity.setCreateAt(new Date());
        taskLogService.save(taskLogEntity);
    }

    @Override
    public void complete(Exchange exchange, ProcessInstanceCompleteRequest request) {
        validateProcessStatus(request.getStatus(), EnumSet.of(ProcessStatusEnum.COMPLETE, ProcessStatusEnum.FAIL));
        ProcessInstanceEntity processInstance = findByID(exchange, request.getId());
        Integer loggedInUserId = AuthenticationUtils.getLoggedInUserId();
        validateProcessStatus(processInstance.getProcessStatus(), EnumSet.of(ProcessStatusEnum.WAITING_FOR_ACKNOWLEDGE));
        validateTaskStates(processInstance, TaskStatusEnum.WAITING_FOR_ACKNOWLEDGE);
        validateUserAccess(processInstance, loggedInUserId);
//        MessageInput context = MessageInputContext.getCurrentContext();
        if (ProcessStatusEnum.COMPLETE.equals(request.getStatus())) {
            updateProcessInstanceForCompletion(processInstance, loggedInUserId, exchange, ProcessStatusEnum.COMPLETE);
            completeTasks(exchange, processInstance, TaskStatusEnum.COMPLETE);
            processInstanceRepository.save(processInstance);
        } else {
            List<ProcessInstanceWatcherEntity> processInstanceWatcherEntities = processInstanceWatcherService.createProcessInstanceWatcherEntity(processInstance, request.getAttribute(), ProcessWatcherEnum.ATTRIBUTE);
            processInstance.getWatcherEntities().addAll(processInstanceWatcherEntities);
            updateProcessInstanceForCompletion(processInstance, loggedInUserId, exchange, ProcessStatusEnum.FAIL);
            completeTasks(exchange, processInstance, TaskStatusEnum.CANCEL);
        }
        processInstanceRepository.save(processInstance);
    }

    @Override
    public void cancelProcess(Exchange exchange, ProcessInstanceCancelRequest request) {
        ValidationUtils.checkNull(request.getId(), () -> new MissingRequiredInputException("processId"));
        ProcessInstanceEntity processInstanceEntity = findByID(exchange, request.getId());
        Integer loggedInUserId = AuthenticationUtils.getLoggedInUserId();
        if (processInstanceEntity.getProcessStatus().equals(ProcessStatusEnum.COMPLETE) || processInstanceEntity.getProcessStatus().equals(ProcessStatusEnum.CANCEL)) {
            throw new InvalidProcessStatusException("status", "Invalid process status.");
        } else if (allowCancelProcess(processInstanceEntity, loggedInUserId)) {
            processInstanceEntity.setProcessStatus(ProcessStatusEnum.CANCEL);
            processInstanceEntity.getTasks().stream().filter(taskEntity -> taskEntity.getTaskStatus().equals(TaskStatusEnum.PENDING)).forEach(taskEntity -> taskEntity.setTaskStatus(TaskStatusEnum.CANCEL));
            processInstanceRepository.save(processInstanceEntity);
        } else {
            throw new ProcessAuthorityException("Cancel Process", "have not permission");
        }
    }

    private void validateProcessStatus(ProcessStatusEnum processStatusEnum, EnumSet<ProcessStatusEnum> enumSet) {
        if (!enumSet.contains(processStatusEnum)) {
            throw new InvalidProcessStatusException("status", "Invalid process status.");
        }
    }

    private void validateTaskStates(ProcessInstanceEntity processInstance, TaskStatusEnum taskStatusEnum) {
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

    private void updateProcessInstanceForCompletion(ProcessInstanceEntity processInstance, Integer loggedInUserId, Exchange exchange, ProcessStatusEnum processStatusEnum) {
        processInstance.setUpdateAt(new Date());
        processInstance.setUpdateBy(loggedInUserId);
        processInstance.setProcessStatus(processStatusEnum);
        processInstance.setLastMessageSequenceId(exchange.getMessage().getHeader(SCM_PARAMETER_CLIENT_CORRELATION_ID, String.class));
    }

    private void completeTasks(Exchange exchange, ProcessInstanceEntity processInstance, TaskStatusEnum taskStatusEnum) {
        processInstance.getTasks().stream().filter(task -> TaskStatusEnum.WAITING_FOR_ACKNOWLEDGE.equals(task.getTaskStatus())).forEach(task -> {
            task.setUpdateAt(new Date());
            task.setTaskStatus(taskStatusEnum);
            taskLogService.mapToTaskLogAndPersist(exchange, task);
        });
    }
}


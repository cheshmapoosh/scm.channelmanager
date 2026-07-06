package ir.daneshrefah.scm.provider.task.service;

import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.exception.AccessDeniedException;
import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.provider.task.constant.ProcessStatusEnum;
import ir.daneshrefah.scm.provider.task.constant.TaskStatusEnum;
import ir.daneshrefah.scm.provider.task.entity.ProcessInstanceEntity;
import ir.daneshrefah.scm.provider.task.entity.TaskEntity;
import ir.daneshrefah.scm.provider.task.exception.InvalidTaskStatusException;
import ir.daneshrefah.scm.provider.task.mapper.TaskMapper;
import ir.daneshrefah.scm.provider.task.model.TaskFilterRequest;
import ir.daneshrefah.scm.provider.task.model.TaskRequest;
import ir.daneshrefah.scm.provider.task.model.TaskResponse;
import ir.daneshrefah.scm.provider.task.repository.ProcessInstanceRepository;
import ir.daneshrefah.scm.provider.task.repository.TaskRepository;
import ir.daneshrefah.scm.provider.task.repository.TaskSpecs;
import ir.daneshrefah.scm.provider.task.utils.PageableUtils;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.utils.string.ArchiveUtils;
import lombok.AllArgsConstructor;
import org.apache.camel.Exchange;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_ACCESS_DENIED;
import static ir.daneshrefah.scm.provider.task.constant.TaskStatusEnum.*;
import static ir.daneshrefah.scm.utils.constant.Constants.SCM_PARAMETER_CLIENT_CORRELATION_ID;

@Service
@AllArgsConstructor
public class TaskManagementServiceImpl implements TaskManagementService {

    private final ProcessInstanceRepository processInstanceRepository;
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final TaskLogService taskLogService;
    private final ProcessManagementService processManagementService;

    private final ProcessTaskDefinitionService processTaskDefinitionService;

    @Override
    public PagedResponseData<TaskResponse> findAllTaskByUserIDAndFilter(Exchange exchange,TaskFilterRequest request) {
        request = Objects.nonNull(request) ? request : new TaskFilterRequest();
        request.setUserId(AuthenticationUtils.getLoggedInUserId());
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageableUtils.getPageable(request, sort);
        Page<TaskEntity> entities = taskRepository.findAll(TaskSpecs.toSpecification(request), pageable);
        List<TaskResponse> taskResponseList = taskMapper.toTaskResponseListWithProcessInstance(entities.stream().toList());
        return new PagedResponseData<>(request.getPageNo(), request.getPageSize(), entities.getTotalElements(), taskResponseList);
    }

    @Override
    public TaskResponse completeTask(Exchange exchange,TaskRequest taskRequest) {
        processTaskDefinitionService.validateTaskBeforeComplete(exchange,taskRequest);
        if (taskRequest.getAction() == null) {
            throw new InvalidInputException("action");
        }

        TaskEntity taskEntity = findTaskByTaskIDAndUserID(taskRequest);

        validateTaskStatus(taskEntity, taskRequest);

        switch (taskRequest.getAction()) {
            case CANCEL -> handleCancellation(exchange,taskEntity);
            case COMPLETE -> handleCompletion(exchange,taskEntity);
            default -> throw new InvalidInputException("Invalid action");
        }
        return taskMapper.toTaskResponseWithProcessInstance(taskEntity);
    }

    private void handleCancellation(Exchange exchange,TaskEntity taskEntity) {
        cancelPendingTasks(taskEntity.getProcessInstance());
        cancelProcessInstance(exchange,taskEntity.getProcessInstance());
        persistTaskLog(exchange,taskEntity);
    }

    private void handleCompletion(Exchange exchange,TaskEntity taskEntity) {
        ProcessInstanceEntity processInstance = taskEntity.getProcessInstance();
        // If all tasks except for the current task and global tasks are complete
        if (allOtherTasksCompleteExceptGlobal(taskEntity)) {
            handleGlobalTaskOrCreateNew(exchange,taskEntity, processInstance);
        } else {
            completeTask(exchange,taskEntity);
        }
    }

    private void handleGlobalTaskOrCreateNew(Exchange exchange,TaskEntity taskEntity, ProcessInstanceEntity processInstance) {
        Optional<TaskEntity> globalTaskOpt = findGlobalTask(processInstance);
        if (globalTaskOpt.isPresent()) {
            TaskEntity globalTask = globalTaskOpt.get();
            globalTask.setGlobal(false);
            globalTask.setTaskStatus(WAITING_FOR_CONFIRM);
            taskEntity.setTaskStatus(COMPLETE);
            taskEntity.setUpdateAt(new Date());
            processInstance.setProcessStatus(ProcessStatusEnum.WAITING_FOR_CONFIRM);
            taskRepository.save(globalTask);
            taskRepository.save(taskEntity);
            processInstanceRepository.save(processInstance);
            persistTaskLog(exchange,globalTask);
            persistTaskLog(exchange,taskEntity);
        } else {
            createOrUpdateConfirmationTask(exchange,taskEntity, processInstance);
        }
    }

    private void createOrUpdateConfirmationTask(Exchange exchange,TaskEntity taskEntity, ProcessInstanceEntity processInstance) {
        if (processInstance.getConfirmUserId() != null) {
            TaskEntity confirmationTask = new TaskEntity();
            confirmationTask.setProcessInstance(processInstance);
            confirmationTask.setUserId(processInstance.getConfirmUserId());

            processInstance.getTasks().stream().filter(task -> task.getUserId().equals(processInstance.getConfirmUserId()))
                    .findFirst()
                    .ifPresent(task -> confirmationTask.setFullName(task.getFullName()));

            confirmationTask.setTaskStatus(WAITING_FOR_CONFIRM);
            confirmationTask.setCreatedBy(AuthenticationUtils.getLoggedInUserId());
            confirmationTask.setCreateAt(new Date());
            confirmationTask.setArchiveNo(ArchiveUtils.calculateOneMonthArchiveNo());
            confirmationTask.setSigner(false);
            processInstance.addTaskEntity(confirmationTask);
            processInstance.setProcessStatus(ProcessStatusEnum.WAITING_FOR_CONFIRM);
            taskEntity.setTaskStatus(COMPLETE);
            taskEntity.setUpdateAt(new Date());
            taskRepository.save(taskEntity);
            processInstanceRepository.save(processInstance);
            persistTaskLog(exchange,taskEntity);
        } else {
            updateTaskAndProcessStatus(exchange,taskEntity, processInstance);
        }
    }

    private void completeTask(Exchange exchange,TaskEntity taskEntity) {
        taskEntity.setTaskStatus(COMPLETE);
        taskEntity.setUpdateAt(new Date());
        taskRepository.save(taskEntity);
        persistTaskLog(exchange,taskEntity);
    }

    private void updateTaskAndProcessStatus(Exchange exchange,TaskEntity taskEntity, ProcessInstanceEntity processInstance) {
        taskEntity.setTaskStatus(TaskStatusEnum.WAITING_FOR_CONFIRM);
        processInstance.setProcessStatus(ProcessStatusEnum.WAITING_FOR_CONFIRM);
        processInstanceRepository.save(processInstance);
        taskRepository.save(taskEntity);
        persistTaskLog(exchange,taskEntity);
    }

    private void persistTaskLog(Exchange exchange,TaskEntity taskEntity) {
        taskLogService.mapToTaskLogAndPersist(exchange,taskEntity);
    }

    private Optional<TaskEntity> findGlobalTask(ProcessInstanceEntity processInstance) {
        return processInstance.getTasks().stream().filter(TaskEntity::isGlobal).findFirst();
    }

    private void validateTaskStatus(TaskEntity taskEntity, TaskRequest taskRequest) {
        if (taskEntity.getTaskStatus().equals(WAITING_FOR_CONFIRM) || taskEntity.isGlobal()) {
            if (!taskRequest.getAction().equals(CANCEL)) {
                throw new InvalidTaskStatusException("status", "Invalid task status.");
            }
        } else if (!taskEntity.getTaskStatus().equals(PENDING)) {
            throw new InvalidTaskStatusException("status", "Invalid task status.");
        }
    }

    private boolean allOtherTasksComplete(TaskEntity taskEntity) {
        return areAllOtherTasksComplete(taskEntity, false);
    }

    private boolean allOtherTasksCompleteExceptGlobal(TaskEntity taskEntity) {
        return areAllOtherTasksComplete(taskEntity, true);
    }

    private boolean areAllOtherTasksComplete(TaskEntity taskEntity, boolean excludeGlobal) {
        return taskEntity.getProcessInstance()
                .getTasks()
                .stream()
                .filter(task -> !Objects.equals(task.getId(), taskEntity.getId())).filter(task -> !(excludeGlobal && task.isGlobal()))
                .allMatch(task -> task.getTaskStatus().equals(COMPLETE));
    }

    private void cancelPendingTasks(ProcessInstanceEntity processInstanceEntity) {
        processInstanceEntity.getTasks().stream()
                .filter(task -> EnumSet.of(PENDING, WAITING_FOR_CONFIRM).contains(task.getTaskStatus()))
                .forEach(task -> {
                    task.setTaskStatus(CANCEL);
                    task.setUpdateAt(new Date());
                });
    }

    private void cancelProcessInstance(Exchange exchange,ProcessInstanceEntity processInstanceEntity) {
        processInstanceEntity.setLastMessageSequenceId(exchange.getMessage().getHeader(SCM_PARAMETER_CLIENT_CORRELATION_ID,String.class));
        processInstanceEntity.setUpdateBy(AuthenticationUtils.getLoggedInUserId());
        processInstanceEntity.setUpdateAt(new Date());
        processInstanceEntity.setProcessStatus(ProcessStatusEnum.CANCEL);
        processInstanceRepository.save(processInstanceEntity);
    }

    private TaskEntity findTaskByTaskIDAndUserID(TaskRequest taskRequest) {
        return taskRepository.findByIdAndUserId(taskRequest.getTaskId(), AuthenticationUtils.getLoggedInUserId())
                .orElseThrow(() -> new NoMatchRecordFoundException("taskID"));
    }

    @Override
    public List<TaskResponse> findAllTasksByProcessId(Exchange exchange,Long processID) {
        ProcessInstanceEntity processInstance = processManagementService.findByID(exchange,processID);
        Integer loggedInUserId = AuthenticationUtils.getLoggedInUserId();
        boolean hasAccess = processInstance.getTasks()
                .stream()
                .anyMatch(task -> loggedInUserId.equals(task.getUserId())) || loggedInUserId.equals(processInstance.getConfirmUserId());
        if (hasAccess) {
            List<TaskEntity> tasks = processInstance.getTasks().stream().filter(TaskEntity::getSigner).toList();
            return taskMapper.toTaskResponseList(tasks);
        }
        throw new AccessDeniedException("processID", ERROR_CODE_ACCESS_DENIED, "User does not have access to tasks.");
    }
}

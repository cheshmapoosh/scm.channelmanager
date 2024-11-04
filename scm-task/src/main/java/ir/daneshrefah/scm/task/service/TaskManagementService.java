package ir.daneshrefah.scm.task.service;

import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.task.model.TaskFilterRequest;
import ir.daneshrefah.scm.task.model.TaskRequest;
import ir.daneshrefah.scm.task.model.TaskResponse;

import java.util.List;

public interface TaskManagementService {

    PagedResponseData<TaskResponse> findAllTaskByUserIDAndFilter(TaskFilterRequest request);

    TaskResponse completeTask(TaskRequest taskRequest);

    List<TaskResponse> findAllTasksByProcessId(Long processID);
}

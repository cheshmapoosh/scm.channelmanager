package ir.daneshrefah.scm.provider.task.service;

import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.provider.task.model.TaskFilterRequest;
import ir.daneshrefah.scm.provider.task.model.TaskRequest;
import ir.daneshrefah.scm.provider.task.model.TaskResponse;
import org.apache.camel.Exchange;

import java.util.List;

public interface TaskManagementService {

    PagedResponseData<TaskResponse> findAllTaskByUserIDAndFilter(Exchange exchange,TaskFilterRequest request);

    TaskResponse completeTask(Exchange exchange,TaskRequest taskRequest);

    List<TaskResponse> findAllTasksByProcessId(Exchange exchange,Long processID);
}

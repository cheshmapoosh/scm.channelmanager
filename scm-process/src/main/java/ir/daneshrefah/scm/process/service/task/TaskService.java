package ir.daneshrefah.scm.process.service.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.process.service.dto.task.TaskCompleteRequest;
import ir.daneshrefah.scm.process.service.dto.task.TaskFindRequest;

public interface TaskService {

    PagedResponseData findTaskList(TaskFindRequest taskFindRequest) throws JsonProcessingException;

    boolean completeTask(TaskCompleteRequest taskRequest) throws JsonProcessingException;

}

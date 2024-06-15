package ir.daneshrefah.scm.process.service.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import ir.daneshrefah.scm.process.service.dto.TaskCompleteRequest;
import ir.daneshrefah.scm.process.model.response.TaskResponse;
import ir.daneshrefah.scm.process.service.dto.TaskFindRequest;

import java.util.List;

public interface TaskService {

    List<TaskResponse> findTaskList(TaskFindRequest taskFindRequest) throws JsonProcessingException;

    boolean completeTask(TaskCompleteRequest taskRequest) throws JsonProcessingException;

}

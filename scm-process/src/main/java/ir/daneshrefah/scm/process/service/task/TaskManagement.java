package ir.daneshrefah.scm.process.service.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import ir.daneshrefah.scm.process.model.request.TaskRequest;
import ir.daneshrefah.scm.process.model.response.TaskResponse;

import java.util.List;

public interface TaskManagement {
    List<TaskResponse> getTaskList(TaskRequest taskRequest) throws JsonProcessingException;

    boolean completeTask(TaskRequest taskRequest) throws JsonProcessingException;
}

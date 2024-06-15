package ir.daneshrefah.scm.process.service.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import ir.daneshrefah.scm.process.model.task.TaskInfo;
import ir.daneshrefah.scm.process.service.dto.TaskCompleteRequest;
import ir.daneshrefah.scm.process.service.dto.TaskFindRequest;

import java.util.List;

public interface TaskService {

    List<TaskInfo> findTaskList(TaskFindRequest taskFindRequest) throws JsonProcessingException;

    boolean completeTask(TaskCompleteRequest taskRequest) throws JsonProcessingException;

}

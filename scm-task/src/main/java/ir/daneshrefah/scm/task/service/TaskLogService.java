package ir.daneshrefah.scm.task.service;

import ir.daneshrefah.scm.task.entity.TaskEntity;
import ir.daneshrefah.scm.task.entity.TaskLogEntity;

public interface TaskLogService {

     TaskLogEntity save(TaskLogEntity taskLogEntity);

     TaskLogEntity mapToTaskLogAndPersist(TaskEntity taskEntity);
}

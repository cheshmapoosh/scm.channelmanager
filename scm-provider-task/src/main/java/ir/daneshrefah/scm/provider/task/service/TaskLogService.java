package ir.daneshrefah.scm.provider.task.service;

import ir.daneshrefah.scm.provider.task.entity.TaskEntity;
import ir.daneshrefah.scm.provider.task.entity.TaskLogEntity;
import org.apache.camel.Exchange;

public interface TaskLogService {

     TaskLogEntity save(TaskLogEntity taskLogEntity);

     TaskLogEntity mapToTaskLogAndPersist(Exchange exchange,TaskEntity taskEntity);
}

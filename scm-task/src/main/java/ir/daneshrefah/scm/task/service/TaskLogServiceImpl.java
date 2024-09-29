package ir.daneshrefah.scm.task.service;

import ir.daneshrefah.scm.common.model.message.MessageInput;
import ir.daneshrefah.scm.task.entity.TaskEntity;
import ir.daneshrefah.scm.task.entity.TaskLogEntity;
import ir.daneshrefah.scm.task.repository.TaskLogRepository;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.utils.MessageInputContext;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@AllArgsConstructor
public class TaskLogServiceImpl implements TaskLogService {

    private final TaskLogRepository taskLogRepository;

    @Override
    public TaskLogEntity save(TaskLogEntity taskLogEntity) {
        return taskLogRepository.save(taskLogEntity);
    }

    @Override
    public TaskLogEntity mapToTaskLogAndPersist(TaskEntity taskEntity) {
        TaskLogEntity taskLogEntity = new TaskLogEntity();
        MessageInput context = MessageInputContext.getCurrentContext();
        taskLogEntity.setLastChannelCode(context.getChannel().getCode());
        taskLogEntity.setTaskEntity(taskEntity);
        taskLogEntity.setStatus(taskEntity.getTaskStatus());
        taskLogEntity.setCreatedBy(AuthenticationUtils.getLoggedInUserId());
        taskLogEntity.setCreateAt(new Date());
        return save(taskLogEntity);
    }
}

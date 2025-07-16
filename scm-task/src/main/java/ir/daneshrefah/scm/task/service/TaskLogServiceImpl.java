package ir.daneshrefah.scm.task.service;

import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.task.entity.TaskEntity;
import ir.daneshrefah.scm.task.entity.TaskLogEntity;
import ir.daneshrefah.scm.task.repository.TaskLogRepository;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.utils.string.ArchiveUtils;
import lombok.AllArgsConstructor;
import org.apache.camel.Exchange;
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
    public TaskLogEntity mapToTaskLogAndPersist(Exchange exchange,TaskEntity taskEntity) {
        TaskLogEntity taskLogEntity = new TaskLogEntity();
        //TODO TEMPORARY GET CHANNEL CODE FROM EXCHANGE
//        MessageInput context = MessageInputContext.getCurrentContext();
//        taskLogEntity.setLastChannelCode(context.getChannel().getCode());
        taskLogEntity.setLastChannelCode(exchange.getProperty(Message.CHANNEL_CODE,String.class));
        taskLogEntity.setTaskEntity(taskEntity);
        taskLogEntity.setStatus(taskEntity.getTaskStatus());
        taskLogEntity.setArchiveNo(ArchiveUtils.calculateOneMonthArchiveNo());
        taskLogEntity.setCreatedBy(AuthenticationUtils.getLoggedInUserId());
        taskLogEntity.setCreateAt(new Date());
        return save(taskLogEntity);
    }
}

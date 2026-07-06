package ir.daneshrefah.scm.task.service;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.task.constant.ProcessWatcherEnum;
import ir.daneshrefah.scm.task.entity.ProcessInstanceEntity;
import ir.daneshrefah.scm.task.entity.ProcessInstanceWatcherEntity;
import ir.daneshrefah.scm.utils.string.ArchiveUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

import static ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils.getLoggedInUserId;

@Service
public class ProcessInstanceWatcherServiceImpl implements ProcessInstanceWatcherService {

    public List<ProcessInstanceWatcherEntity> createProcessInstanceWatcherEntity(ProcessInstanceEntity processInstanceEntity, JsonNode data, ProcessWatcherEnum processWatcherEnum) {
        //TODO:Split into rows based on chunk size of data column
        ProcessInstanceWatcherEntity processInstanceWatcherEntity = new ProcessInstanceWatcherEntity();
        processInstanceWatcherEntity.setUserId(getLoggedInUserId());
        processInstanceWatcherEntity.setProcessInstance(processInstanceEntity);
        processInstanceWatcherEntity.setArchiveNo(ArchiveUtils.calculateOneMonthArchiveNo());
        processInstanceWatcherEntity.setData(data);
        processInstanceWatcherEntity.setType(processWatcherEnum);
        processInstanceWatcherEntity.setCreateBy(getLoggedInUserId());
        processInstanceWatcherEntity.setCreateAt(new Date());
        processInstanceWatcherEntity.setRowNo(0);
        return List.of(processInstanceWatcherEntity);
    }
}

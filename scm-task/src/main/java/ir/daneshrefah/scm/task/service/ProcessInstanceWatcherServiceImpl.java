package ir.daneshrefah.scm.task.service;

import ir.daneshrefah.scm.task.constant.ProcessStatusEnum;
import ir.daneshrefah.scm.task.entity.ProcessInstanceEntity;
import ir.daneshrefah.scm.task.entity.ProcessInstanceWatcherEntity;
import ir.daneshrefah.scm.task.repository.ProcessInstanceWatcherRepository;
import ir.daneshrefah.scm.utils.string.ArchiveUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

import static ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils.getLoggedInUserId;

@Service
public class ProcessInstanceWatcherServiceImpl implements ProcessInstanceWatcherService {

    @Autowired
    private ProcessInstanceWatcherRepository processInstanceWatcherRepository;

    public ProcessInstanceWatcherEntity createProcessInstanceWatcherEntity(ProcessInstanceEntity processInstance, ProcessStatusEnum processStatus) {
        ProcessInstanceWatcherEntity processInstanceWatcherEntity = new ProcessInstanceWatcherEntity();
        processInstanceWatcherEntity.setUserId(getLoggedInUserId());
        processInstanceWatcherEntity.setProcessInstance(processInstance);
        processInstanceWatcherEntity.setArchiveNo(ArchiveUtils.calculateOneMonthArchiveNo());
        processInstanceWatcherEntity.setCreateBy(getLoggedInUserId());
        processInstanceWatcherEntity.setCreateAt(new Date());
        return processInstanceWatcherEntity;
    }

    public ProcessInstanceWatcherEntity persist(ProcessInstanceEntity processInstance, ProcessStatusEnum processStatus) {
        ProcessInstanceWatcherEntity processInstanceWatcherEntity = createProcessInstanceWatcherEntity(processInstance, processStatus);
        return processInstanceWatcherRepository.save(processInstanceWatcherEntity);
    }
}

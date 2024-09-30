package ir.daneshrefah.scm.task.service;

import ir.daneshrefah.scm.task.constant.ProcessStatusEnum;
import ir.daneshrefah.scm.task.entity.ProcessInstanceEntity;
import ir.daneshrefah.scm.task.entity.ProcessInstanceWatcherEntity;

public interface ProcessInstanceWatcherService {

     ProcessInstanceWatcherEntity createProcessInstanceWatcherEntity(ProcessInstanceEntity processInstance,ProcessStatusEnum processStatus);

     ProcessInstanceWatcherEntity persist(ProcessInstanceEntity processInstance, ProcessStatusEnum processStatus);
}

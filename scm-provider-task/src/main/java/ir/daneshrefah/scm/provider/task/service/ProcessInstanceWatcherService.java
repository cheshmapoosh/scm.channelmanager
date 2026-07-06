package ir.daneshrefah.scm.provider.task.service;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.provider.task.constant.ProcessWatcherEnum;
import ir.daneshrefah.scm.provider.task.entity.ProcessInstanceEntity;
import ir.daneshrefah.scm.provider.task.entity.ProcessInstanceWatcherEntity;

import java.util.List;

public interface ProcessInstanceWatcherService {
    List<ProcessInstanceWatcherEntity> createProcessInstanceWatcherEntity(ProcessInstanceEntity processInstanceEntity, JsonNode data, ProcessWatcherEnum processWatcherEnum);
}
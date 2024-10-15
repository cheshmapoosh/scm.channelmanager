package ir.daneshrefah.scm.task.service;

import ir.daneshrefah.scm.task.constant.DefinitionTypeEnum;
import ir.daneshrefah.scm.task.constant.ExecutionMethodTypeEnum;
import ir.daneshrefah.scm.task.constant.ProcessCodeEnum;
import ir.daneshrefah.scm.task.constant.ProcessNameEnum;
import ir.daneshrefah.scm.task.entity.ProcessTaskDefinitionEntity;
import ir.daneshrefah.scm.task.model.ProcessInstanceApproveRequest;
import ir.daneshrefah.scm.task.model.ProcessInstanceStartRequest;
import ir.daneshrefah.scm.task.model.TaskRequest;

public interface ProcessTaskDefinitionService {

    ProcessTaskDefinitionEntity findProcessTaskDefinitionEntity(ProcessNameEnum processName, ExecutionMethodTypeEnum executionMethodType, DefinitionTypeEnum definitionType);

    ProcessTaskDefinitionEntity findProcessTaskDefinitionEntity(ProcessNameEnum processName, ExecutionMethodTypeEnum executionMethodType, DefinitionTypeEnum definitionType, ProcessCodeEnum processCode);

    void verifySecondAuthentication(ProcessNameEnum processName, ExecutionMethodTypeEnum executionMethodType, DefinitionTypeEnum definitionType, ProcessCodeEnum processCode);

     void validateProcessBeforeStart(ProcessInstanceStartRequest request);
     void validateProcessBeforeApprove(ProcessInstanceApproveRequest request);
     void validateTaskBeforeComplete(TaskRequest taskRequest);

}

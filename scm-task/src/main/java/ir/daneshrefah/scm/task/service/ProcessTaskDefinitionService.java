package ir.daneshrefah.scm.task.service;

import ir.daneshrefah.scm.task.constant.DefinitionTypeEnum;
import ir.daneshrefah.scm.task.constant.ExecutionMethodTypeEnum;
import ir.daneshrefah.scm.task.constant.ProcessCodeEnum;
import ir.daneshrefah.scm.task.constant.ProcessNameEnum;
import ir.daneshrefah.scm.task.entity.ProcessTaskDefinitionEntity;
import ir.daneshrefah.scm.task.model.ProcessInstanceApproveRequest;
import ir.daneshrefah.scm.task.model.ProcessInstanceStartRequest;
import ir.daneshrefah.scm.task.model.TaskRequest;
import org.apache.camel.Exchange;

public interface ProcessTaskDefinitionService {

    ProcessTaskDefinitionEntity findProcessTaskDefinitionEntity(ProcessNameEnum processName, ExecutionMethodTypeEnum executionMethodType, DefinitionTypeEnum definitionType);

    ProcessTaskDefinitionEntity findProcessTaskDefinitionEntity(ProcessNameEnum processName, ExecutionMethodTypeEnum executionMethodType, DefinitionTypeEnum definitionType, ProcessCodeEnum processCode);

    void verifySecondAuthentication(Exchange exchange, ProcessNameEnum processName, ExecutionMethodTypeEnum executionMethodType, DefinitionTypeEnum definitionType, ProcessCodeEnum processCode);

     void validateProcessBeforeStart(Exchange exchange,ProcessInstanceStartRequest request);
     void validateProcessBeforeApprove(Exchange exchange,ProcessInstanceApproveRequest request);
     void validateTaskBeforeComplete(Exchange exchange,TaskRequest taskRequest);

}

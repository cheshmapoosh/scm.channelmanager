package ir.daneshrefah.scm.provider.task.service;

import ir.daneshrefah.scm.provider.task.constant.DefinitionTypeEnum;
import ir.daneshrefah.scm.provider.task.constant.ExecutionMethodTypeEnum;
import ir.daneshrefah.scm.provider.task.constant.ProcessCodeEnum;
import ir.daneshrefah.scm.provider.task.constant.ProcessNameEnum;
import ir.daneshrefah.scm.provider.task.entity.ProcessTaskDefinitionEntity;
import ir.daneshrefah.scm.provider.task.model.ProcessInstanceApproveRequest;
import ir.daneshrefah.scm.provider.task.model.ProcessInstanceStartRequest;
import ir.daneshrefah.scm.provider.task.model.TaskRequest;
import org.apache.camel.Exchange;

public interface ProcessTaskDefinitionService {

    ProcessTaskDefinitionEntity findProcessTaskDefinitionEntity(ProcessNameEnum processName, ExecutionMethodTypeEnum executionMethodType, DefinitionTypeEnum definitionType);

    ProcessTaskDefinitionEntity findProcessTaskDefinitionEntity(ProcessNameEnum processName, ExecutionMethodTypeEnum executionMethodType, DefinitionTypeEnum definitionType, ProcessCodeEnum processCode);

    void verifySecondAuthentication(Exchange exchange, ProcessNameEnum processName, ExecutionMethodTypeEnum executionMethodType, DefinitionTypeEnum definitionType, ProcessCodeEnum processCode);

     void validateProcessBeforeStart(Exchange exchange,ProcessInstanceStartRequest request);
     void validateProcessBeforeApprove(Exchange exchange,ProcessInstanceApproveRequest request);
     void validateTaskBeforeComplete(Exchange exchange,TaskRequest taskRequest);

}

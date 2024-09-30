package ir.daneshrefah.scm.task.service;

import ir.daneshrefah.scm.task.constant.DefinitionTypeEnum;
import ir.daneshrefah.scm.task.constant.ExecutionMethodTypeEnum;
import ir.daneshrefah.scm.task.constant.ProcessCodeEnum;
import ir.daneshrefah.scm.task.constant.ProcessNameEnum;
import ir.daneshrefah.scm.task.entity.ProcessTaskDefinitionEntity;

public interface ProcessTaskDefinitionService {

    ProcessTaskDefinitionEntity findProcessTaskDefinitionEntity(ProcessNameEnum processName, ExecutionMethodTypeEnum executionMethodType, DefinitionTypeEnum definitionType);

    ProcessTaskDefinitionEntity findProcessTaskDefinitionEntity(ProcessNameEnum processName, ExecutionMethodTypeEnum executionMethodType, DefinitionTypeEnum definitionType, ProcessCodeEnum processCode);

    void verifySecondAuthentication(ProcessNameEnum processName, ExecutionMethodTypeEnum executionMethodType, DefinitionTypeEnum definitionType, ProcessCodeEnum processCode, String otpCode);

}

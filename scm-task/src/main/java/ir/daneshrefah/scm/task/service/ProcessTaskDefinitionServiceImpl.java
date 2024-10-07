package ir.daneshrefah.scm.task.service;

import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.model.message.MessageInput;
import ir.daneshrefah.scm.task.constant.DefinitionTypeEnum;
import ir.daneshrefah.scm.task.constant.ExecutionMethodTypeEnum;
import ir.daneshrefah.scm.task.constant.ProcessCodeEnum;
import ir.daneshrefah.scm.task.constant.ProcessNameEnum;
import ir.daneshrefah.scm.task.entity.ProcessTaskDefinitionEntity;
import ir.daneshrefah.scm.task.repository.ProcessTaskDefinitionRepository;
import ir.daneshrefah.scm.utils.MessageInputContext;
import ir.daneshrefah.scm.utils.validation.ChainValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static ir.daneshrefah.scm.utils.constant.Constants.SCM_PARAMETER_CLAIM_CODE;

@Service
@RequiredArgsConstructor
public class ProcessTaskDefinitionServiceImpl implements ProcessTaskDefinitionService { //TODO complete and test this service after otp is complete

    private final ProcessTaskDefinitionRepository processTaskDefinitionRepository;

    @Override
    public ProcessTaskDefinitionEntity findProcessTaskDefinitionEntity(ProcessNameEnum processName, ExecutionMethodTypeEnum executionMethodType, DefinitionTypeEnum definitionType, ProcessCodeEnum processCode) {
        ChainValidation.crateValidator(processName, "processName").checkNull();
        ChainValidation.crateValidator(executionMethodType, "executionMethodType").checkNull();
        ChainValidation.crateValidator(definitionType, "definitionType").checkNull();
        return processTaskDefinitionRepository.findByProcessNameAndExecutionMethodTypeAndDefinitionTypeAndProcessCode(processName.name(), executionMethodType.getCode(), definitionType.getCode(), processCode)
                .orElseThrow(() -> new NoMatchRecordFoundException("processName~executionCode~definitionCode~processCode"));
    }

    @Override
    public ProcessTaskDefinitionEntity findProcessTaskDefinitionEntity(ProcessNameEnum processName, ExecutionMethodTypeEnum executionMethodType, DefinitionTypeEnum definitionType) {
        return processTaskDefinitionRepository.findByProcessNameAndExecutionMethodTypeAndDefinitionType(processName.name(), executionMethodType.getCode(), definitionType.getCode())
                .orElseThrow(() -> new NoMatchRecordFoundException("processName~executionCode~definitionCode~processCode"));
    }

    public void verifySecondAuthentication(ProcessNameEnum processName, ExecutionMethodTypeEnum executionMethodType, DefinitionTypeEnum definitionType, ProcessCodeEnum processCode) {
//        ProcessTaskDefinitionEntity processTaskDefinitionEntity = findProcessTaskDefinitionEntity(processName, executionMethodType, definitionType, processCode);
//        if (processTaskDefinitionEntity.getUserAccessSecondAuth() > 0) {
//        boolean isValid = true;// verify otp
//        if (!isValid) {
//
//        }
        MessageInput messageInput = MessageInputContext.getCurrentContext();
        String otpCode = messageInput.getHeader(SCM_PARAMETER_CLAIM_CODE);
        if (!otpCode.equals("123")) {
            throw new RuntimeException();
        }

//        }
    }
}

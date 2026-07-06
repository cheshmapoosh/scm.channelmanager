package ir.daneshrefah.scm.task.service;

import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.otp.service.OtpClientService;
import ir.daneshrefah.scm.task.constant.DefinitionTypeEnum;
import ir.daneshrefah.scm.task.constant.ExecutionMethodTypeEnum;
import ir.daneshrefah.scm.task.constant.ProcessCodeEnum;
import ir.daneshrefah.scm.task.constant.ProcessNameEnum;
import ir.daneshrefah.scm.task.entity.ProcessTaskDefinitionEntity;
import ir.daneshrefah.scm.task.model.ProcessInstanceApproveRequest;
import ir.daneshrefah.scm.task.model.ProcessInstanceStartRequest;
import ir.daneshrefah.scm.task.model.TaskRequest;
import ir.daneshrefah.scm.task.repository.ProcessTaskDefinitionRepository;
import ir.daneshrefah.scm.utils.validation.ChainValidation;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import static ir.daneshrefah.scm.utils.constant.Constants.*;

@Service
@RequiredArgsConstructor
public class ProcessTaskDefinitionServiceImpl implements ProcessTaskDefinitionService {

    private final ProcessTaskDefinitionRepository processTaskDefinitionRepository;
    private final OtpClientService otpClientService;
    private final CacheManager cacheManager;
    public static final String CACHE_NAME_OTP = "task_definition_otp";

    @Override
    public ProcessTaskDefinitionEntity findProcessTaskDefinitionEntity(ProcessNameEnum processName, ExecutionMethodTypeEnum executionMethodType, DefinitionTypeEnum definitionType, ProcessCodeEnum processCode) {
        return processTaskDefinitionRepository.findByProcessNameAndExecutionMethodTypeAndDefinitionTypeAndProcessCode(processName, executionMethodType, definitionType, processCode)
                .orElseThrow(() -> new NoMatchRecordFoundException("processName~executionCode~definitionCode~processCode"));
    }

    @Override
    public ProcessTaskDefinitionEntity findProcessTaskDefinitionEntity(ProcessNameEnum processName, ExecutionMethodTypeEnum executionMethodType, DefinitionTypeEnum definitionType) {
        return processTaskDefinitionRepository.findByProcessNameAndExecutionMethodTypeAndDefinitionType(processName.name(), executionMethodType.getCode(), definitionType.getCode())
                .orElseThrow(() -> new NoMatchRecordFoundException("processName~executionCode~definitionCode~processCode"));
    }

    @Override
    public void verifySecondAuthentication(Exchange exchange,ProcessNameEnum processName, ExecutionMethodTypeEnum executionMethodType, DefinitionTypeEnum definitionType, ProcessCodeEnum processCode) {
        ChainValidation.crateValidator(processName, "processName").checkNull();
        ChainValidation.crateValidator(processCode, "processCode").checkNull();
        ProcessTaskDefinitionEntity processTaskDefinitionEntity = getProcessTaskDefinitionEntityFromCache(processName, executionMethodType, definitionType, processCode);
        if (processTaskDefinitionEntity.isUserAccessSecondAuth()) {
            //TODO TEMPORARY GET CHANNEL CODE FROM EXCHANGE
//            MessageInput messageInput = MessageInputContext.getCurrentContext();
            String otpCode = exchange.getMessage().getHeader(SCM_PARAMETER_CLAIM_CODE,String.class);
            String authorization = exchange.getMessage().getHeader(SCM_PARAMETER_AUTHORIZATION,String.class);
            String accessParameter = exchange.getMessage().getHeader(SCM_PARAMETER_ACCESS_PARAMETER,String.class);
            otpClientService.verifyOtpOrStaticPasswordLoggedInUserWithException(authorization, otpCode, processTaskDefinitionEntity.getOtpReason(), accessParameter);
        }
    }

    private ProcessTaskDefinitionEntity getProcessTaskDefinitionEntityFromCache(ProcessNameEnum processName, ExecutionMethodTypeEnum executionMethodType, DefinitionTypeEnum definitionType, ProcessCodeEnum processCode) {
        String key = processName.getProcessName() + "~" + executionMethodType.getCode() + "~" + definitionType.getCode() + "~" + processCode.getCode();
        Cache cache = taskDefinitionCache();
        ProcessTaskDefinitionEntity processTaskDefinitionEntity = cache.get(key, ProcessTaskDefinitionEntity.class);
        if (processTaskDefinitionEntity == null) {
            processTaskDefinitionEntity = findProcessTaskDefinitionEntity(processName, executionMethodType, definitionType, processCode);
            cache.put(key, processTaskDefinitionEntity);
        }
        return processTaskDefinitionEntity;
    }

    private Cache taskDefinitionCache() {
        Cache cache = cacheManager.getCache(CACHE_NAME_OTP);
        if (cache == null) {
            throw new IllegalStateException("Spring cache is not configured: " + CACHE_NAME_OTP);
        }
        return cache;
    }

    @Override
    public void validateProcessBeforeStart(Exchange exchange,ProcessInstanceStartRequest request) {
        verifySecondAuthentication(exchange,request.getProcessName(), ExecutionMethodTypeEnum.START_PROCESS, DefinitionTypeEnum.PROCESS, request.getProcessCode());
    }

    @Override
    public void validateTaskBeforeComplete(Exchange exchange,TaskRequest request) {
        verifySecondAuthentication(exchange,request.getProcessName(), ExecutionMethodTypeEnum.COMPLETE_TASK, DefinitionTypeEnum.TASK, request.getProcessCode());
    }

    @Override
    public void validateProcessBeforeApprove(Exchange exchange,ProcessInstanceApproveRequest request) {
        verifySecondAuthentication(exchange,request.getProcessName(), ExecutionMethodTypeEnum.APPROVE_PROCESS, DefinitionTypeEnum.PROCESS, request.getProcessCode());
    }
}

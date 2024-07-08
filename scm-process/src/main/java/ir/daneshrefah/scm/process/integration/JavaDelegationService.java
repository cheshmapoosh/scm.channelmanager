package ir.daneshrefah.scm.process.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.IssuerInfo;
import ir.daneshrefah.scm.process.model.ProcessMessage;
import ir.daneshrefah.scm.process.service.constant.ProcessConstants;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class JavaDelegationService implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {ProcessServiceInvoker processServiceInvoker = ProcessServiceInvoker.getInstance();
        ObjectMapper objectMapper = new ObjectMapper();
        IssuerInfo issuerInfo = (IssuerInfo) execution.getVariable(ProcessConstants.ISSUER_INFO);
        if (issuerInfo == null) {
            throw new Exception();//TODO throws Exception
        }
        ProcessMessage processMessage = new ProcessMessage();
        processMessage.setDelegateUsername("");
        processMessage.setCorrelationId(issuerInfo.getParentCorrelationId());
        processMessage.setProcessCorrelationId(UUID.randomUUID().toString());
        processMessage.setProcessDefinitionKey(((ExecutionEntity) execution).getProcessDefinition().getName());
        processMessage.setProcessInstanceId(execution.getProcessInstanceId());
        processMessage.setTaskId(execution.getId());
        processMessage.setTaskName(execution.getCurrentActivityName());
        processMessage.setAccessParameter("123");//TODO:what is access parameter?
        String ServiceCode = "SVC_NAB_CUSTOMER_ACCOUNT_LIST"; //TODO read it from extension property
        processServiceInvoker.callService(ServiceCode, objectMapper.readTree("{}"),processMessage);
    }
}

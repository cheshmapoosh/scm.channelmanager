//package ir.daneshrefah.scm.process.integration;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import ir.daneshrefah.scm.common.model.message.IssuerInfo;
//import ir.daneshrefah.scm.common.model.message.ProcessMessageInput;
//import ir.daneshrefah.scm.process.service.constant.ProcessConstants;
//import org.camunda.bpm.engine.delegate.DelegateExecution;
//import org.camunda.bpm.engine.delegate.JavaDelegate;
//import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
//import org.springframework.stereotype.Component;
//
//@Component
//public class JavaDelegationService implements JavaDelegate {
//
//    @Override
//    public void execute(DelegateExecution execution) throws Exception {ProcessServiceInvoker processServiceInvoker = ProcessServiceInvoker.getInstance();
//        ObjectMapper objectMapper = new ObjectMapper();
//        IssuerInfo issuerInfo = (IssuerInfo) execution.getVariable(ProcessConstants.ISSUER_INFO);
//        if (issuerInfo == null) {
//            throw new Exception();//TODO throws Exception
//        }
//        ProcessMessageInput processMessageInput = ProcessMessageInput.builder()
//                .taskId(execution.getId())
//                .taskName(execution.getCurrentActivityName())
//                .processInstanceId(execution.getProcessInstanceId())
//                .processDefinitionKey(((ExecutionEntity) execution).getProcessDefinition().getName())
//                .build();
//        String ServiceCode = "SVC_NAB_CUSTOMER_ACCOUNT_LIST"; //TODO read it from extension property
//        processServiceInvoker.callService(ServiceCode, objectMapper.readTree("{}"),processMessageInput);
//    }
//}

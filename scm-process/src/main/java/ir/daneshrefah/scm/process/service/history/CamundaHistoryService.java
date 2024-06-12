package ir.daneshrefah.scm.process.service.history;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.data.entity.person.IndividualPersonEntity;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.process.model.CancelProcess;
import ir.daneshrefah.scm.process.model.constant.ProcessState;
import ir.daneshrefah.scm.process.model.constant.TaskState;
import ir.daneshrefah.scm.process.model.filter.ProcessFilter;
import ir.daneshrefah.scm.process.model.request.HistoryRequest;
import ir.daneshrefah.scm.process.model.response.ProcessHistoryResponse;
import ir.daneshrefah.scm.process.model.response.TaskHistoryResponse;
import ir.daneshrefah.scm.process.service.process.CamundaProcessService;
import ir.daneshrefah.scm.process.service.util.CamundaProcessUtil;
import ir.daneshrefah.scm.process.service.util.UserAuthUtils;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.model.bpmn.instance.Collaboration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.*;

@Service
public class CamundaHistoryService implements HistoryManagement {

    public static final String CANCEL_PROCESS = "cancelProcess1";
    @Autowired
    private HistoryService historyService;

    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private CamundaProcessUtil camundaProcessUtil;

    @Autowired
    private RestTemplate restTemplate;

//    @Autowired
//    private PersonService personService;

    @Override
    public List<TaskHistoryResponse> findTaskHistories(HistoryRequest historyRequest) throws JsonProcessingException {
        List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery().processInstanceId(historyRequest.getRootProcessInstanceId()).list();
        return createTaskHistories(historicTaskInstances);
    }

    @Override
    public List<ProcessHistoryResponse> findProcessHistories(ProcessFilter processFilter) throws JsonProcessingException {
        String loggedInUserNationalCode = processFilter.getNationalCode();
//        String loggedInUserNationalCode = UserAuthUtils.getLoggedInUserNationalCode();
        HistoricProcessInstanceQuery historicProcessInstanceQuery = historyService.createHistoricProcessInstanceQuery().or();
        historicProcessInstanceQuery.variableValueLike("users", loggedInUserNationalCode);
        if (processFilter.getStartDate() != null) {
            ValidationUtils.checkNumericInput(processFilter.getStartDate(), () -> new MissingRequiredInputException("id"));//TODO change exception
            historicProcessInstanceQuery.startedAfter(Date.from(Instant.ofEpochMilli(processFilter.getStartDate())));
        }
        if (processFilter.getEndDate() != null) {
            ValidationUtils.checkNumericInput(processFilter.getEndDate(), () -> new MissingRequiredInputException("id"));//TODO change exception
            historicProcessInstanceQuery.startedBefore(Date.from(Instant.ofEpochMilli(processFilter.getEndDate())));
        }
//        if (processFilter.getFilters() != null) {//TODO find the way for nested search
//            for (Filter filter : processFilter.getFilters()) {
//                if (filter.getName() != null && filter.getValue() != null && StringUtils.isNotBlank(filter.getName()) && StringUtils.isNotBlank(filter.getValue())) {
//                    switch (filter.getOperation()) {
//                        case LIKE ->
//                                historicProcessInstanceQuery.variableValueLike(filter.getName(), filter.getValue());
//                        case EQ ->
//                                historicProcessInstanceQuery.variableValueEquals(filter.getName(), filter.getValue());
//                        case GT ->
//                                historicProcessInstanceQuery.variableValueGreaterThan(filter.getName(), filter.getValue());
//                        case GTE ->
//                                historicProcessInstanceQuery.variableValueGreaterThanOrEqual(filter.getName(), filter.getValue());
//                        case LT ->
//                                historicProcessInstanceQuery.variableValueLessThan(filter.getName(), filter.getValue());
//                        case LTE ->
//                                historicProcessInstanceQuery.variableValueLessThanOrEqual(filter.getName(), filter.getValue());
//                    }
//                }
//            }
//        }
        if (processFilter.isActiveProcess()) {
            historicProcessInstanceQuery.active().unfinished();
        } else {
            historicProcessInstanceQuery.finished().completed();
        }

        return createProcessHistories(historicProcessInstanceQuery.endOr().list());
    }

    private List<ProcessHistoryResponse> createProcessHistories(List<HistoricProcessInstance> historicProcessInstances) throws JsonProcessingException {
        String loggedInUserNationalCode = UserAuthUtils.getLoggedInUserNationalCode();
        List<ProcessHistoryResponse> processHistoryResponseList = new ArrayList<>();
        for (HistoricProcessInstance historicProcessInstance : historicProcessInstances) {
            ProcessHistoryResponse processHistoryResponse = new ProcessHistoryResponse();//TODO use mapstruct
            processHistoryResponse.setId(historicProcessInstance.getId());
            processHistoryResponse.setRootProcessInstanceId(historicProcessInstance.getRootProcessInstanceId());
            processHistoryResponse.setDurationInMillis(historicProcessInstance.getDurationInMillis());
            processHistoryResponse.setStartTime(historicProcessInstance.getStartTime());
            processHistoryResponse.setStartTimeMills(historicProcessInstance.getStartTime() != null ? historicProcessInstance.getStartTime().getTime() : null);
            processHistoryResponse.setEndTime(historicProcessInstance.getEndTime());
            processHistoryResponse.setEndTimeMills(historicProcessInstance.getEndTime() != null ? historicProcessInstance.getStartTime().getTime() : null);
            processHistoryResponse.setRemovalTime(historicProcessInstance.getRemovalTime());
            processHistoryResponse.setRemovalTimeMills(historicProcessInstance.getRemovalTime() != null ? historicProcessInstance.getRemovalTime().getTime() : null);
            processHistoryResponse.setStartActivityId(historicProcessInstance.getStartActivityId());
            processHistoryResponse.setState(historicProcessInstance.getState());
            processHistoryResponse.setStateName(ProcessState.getStatusDescription(historicProcessInstance.getState()));
            processHistoryResponse.setProcessName(historicProcessInstance.getProcessDefinitionName());
            processHistoryResponse.setProcessPersianName(historicProcessInstance.getProcessDefinitionKey()); //TODO use resource bundle
            Map<String, Object> dataVariables = getProcessVariables(historicProcessInstance.getId());
            ProcessDefinition processDefinition = camundaProcessUtil.getProcessDefinition(historicProcessInstance.getProcessDefinitionId());
            Map<String, String> extensionProperties = camundaProcessUtil.getExtensionProperties(processDefinition, Collaboration.class);
            if (extensionProperties.containsKey(CANCEL_PROCESS)) {//TODO extract to method
                List<HistoricVariableInstance> historicVariableInstanceList = historyService
                        .createHistoricVariableInstanceQuery()
                        .processInstanceId(historicProcessInstance.getRootProcessInstanceId())
                        .list();
                String value = extensionProperties.get(CANCEL_PROCESS);
                ObjectMapper objectMapper = new ObjectMapper();
                CancelProcess cancelProcess = objectMapper.readValue(value, CancelProcess.class);
                for (String userPermission : cancelProcess.getPermission()) {
                    for (HistoricVariableInstance historicVariableInstance : historicVariableInstanceList) {
                        if (historicVariableInstance.getName().equalsIgnoreCase(userPermission)) {
                            if (historicVariableInstance.getValue().equals(loggedInUserNationalCode)) {
                                dataVariables.put("actions", cancelProcess.getActions());
                            }
                        }
                    }
                }
            }
            processHistoryResponse.setData(dataVariables);
            processHistoryResponseList.add(processHistoryResponse);
        }
        return processHistoryResponseList;
    }

    public IndividualPersonEntity findPerson(String nationalCode) throws JsonProcessingException {
//        if (nationalCode == null) {
//            return null;
//        }
//        IndividualPersonEntity person = personServiceDatabaseImpl.findPersonByNationalCode(nationalCode);
//        if (person != null) {
//            return person;
//        }
        //TODO How Call cif
        return null;
    }

    private List<TaskHistoryResponse> createTaskHistories(List<HistoricTaskInstance> historicTaskInstances) throws JsonProcessingException {
        List<TaskHistoryResponse> taskHistoryResponses = new ArrayList<>();
        for (HistoricTaskInstance historicTaskInstance : historicTaskInstances) {
            TaskHistoryResponse taskHistoryResponse = new TaskHistoryResponse();//TODO use mapstruct
            taskHistoryResponse.setId(historicTaskInstance.getId());
            taskHistoryResponse.setOwner(historicTaskInstance.getOwner());
            taskHistoryResponse.setAssignee(historicTaskInstance.getAssignee());
            findPerson(historicTaskInstance.getAssignee());
            taskHistoryResponse.setParentTaskId(historicTaskInstance.getParentTaskId());
            taskHistoryResponse.setTaskName(historicTaskInstance.getName());
            taskHistoryResponse.setTaskPersianName(historicTaskInstance.getTaskDefinitionKey()); //TODO use resource bundle
            taskHistoryResponse.setDescription(historicTaskInstance.getDescription());
            taskHistoryResponse.setStartTime(historicTaskInstance.getStartTime());
            taskHistoryResponse.setStartTimeMillis(historicTaskInstance.getStartTime() != null ? historicTaskInstance.getStartTime().getTime() : null);
            taskHistoryResponse.setEndTime(historicTaskInstance.getEndTime());
            taskHistoryResponse.setEndTimeMillis(historicTaskInstance.getEndTime() != null ? historicTaskInstance.getEndTime().getTime() : null);
            taskHistoryResponse.setExecutionId(historicTaskInstance.getExecutionId());
            taskHistoryResponse.setProcessInstanceId(historicTaskInstance.getProcessInstanceId());
            taskHistoryResponse.setRootProcessInstanceId(historicTaskInstance.getRootProcessInstanceId());
            taskHistoryResponse.setTaskDefinitionKey(historicTaskInstance.getTaskDefinitionKey());
            taskHistoryResponse.setProcessDefinitionKey(historicTaskInstance.getProcessDefinitionKey());
            taskHistoryResponse.setDeleteReason(historicTaskInstance.getDeleteReason());
            taskHistoryResponse.setState(historicTaskInstance.getDeleteReason());
            taskHistoryResponse.setStateName(TaskState.getStateDescription(historicTaskInstance.getDeleteReason()));
            taskHistoryResponse.setData(getVariablesTask(historicTaskInstance.getRootProcessInstanceId()));
            taskHistoryResponses.add(taskHistoryResponse);
        }
        return taskHistoryResponses;
    }

    //TODO merge and refactor  method
    private Map<String, Object> getProcessVariables(String processInstanceId) {
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> businessData = new HashMap<>();
        List<HistoricVariableInstance> historicVariableInstanceList = historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstanceId).list();

        for (HistoricVariableInstance historicVariableInstance : historicVariableInstanceList) {
            String variable = historicVariableInstance.getName();
            if (variable.startsWith(CamundaProcessService.BUSINESS_DATA)) {
                String key = variable.replace("%s_".formatted(CamundaProcessService.BUSINESS_DATA), "");
                businessData.put(key, historicVariableInstance.getValue());
            } else {
                data.put(variable, historicVariableInstance.getValue());
            }
        }
        data.put(CamundaProcessService.BUSINESS_DATA, businessData);
        return data;
    }

    //TODO merge and refactor  method
    private Map<String, Object> getVariablesTask(String processInstanceId) {
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> businessData = new HashMap<>();
        List<HistoricVariableInstance> historicVariableInstanceList = historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstanceId).list();

        for (HistoricVariableInstance historicVariableInstance : historicVariableInstanceList) {
            String variable = historicVariableInstance.getName();
            if (variable.startsWith(CamundaProcessService.BUSINESS_DATA)) {
                String key = variable.replace("%s_".formatted(CamundaProcessService.BUSINESS_DATA), "");
                businessData.put(key, historicVariableInstance.getValue());
            } else {
                data.put(variable, historicVariableInstance.getValue());
            }
        }
        data.put(CamundaProcessService.BUSINESS_DATA, businessData);
        return data;
    }
}

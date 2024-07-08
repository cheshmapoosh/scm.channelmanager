package ir.daneshrefah.scm.process.service.history;

import com.fasterxml.jackson.core.JsonProcessingException;
import ir.daneshrefah.scm.common.constant.AccessibleLocale;
import ir.daneshrefah.scm.common.data.entity.person.GeneralRealPersonEntity;
import ir.daneshrefah.scm.common.data.service.bundle.ResourceBundleService;
import ir.daneshrefah.scm.common.data.service.person.PersonService;
import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.process.model.process.ProcessInstanceInfo;
import ir.daneshrefah.scm.process.model.task.Assignment;
import ir.daneshrefah.scm.process.model.task.HistoryTaskInfo;
import ir.daneshrefah.scm.process.service.dto.history.HistoryProcessRequest;
import ir.daneshrefah.scm.process.service.dto.history.HistoryProcessResponse;
import ir.daneshrefah.scm.process.service.dto.history.HistoryTaskRequest;
import ir.daneshrefah.scm.process.service.dto.history.HistoryTaskResponse;
import ir.daneshrefah.scm.process.service.util.BpmnExtensionExtractor;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.AllArgsConstructor;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.*;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.model.bpmn.instance.Collaboration;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static ir.daneshrefah.scm.process.service.constant.ProcessConstants.*;

@Service
@AllArgsConstructor
public class CamundaHistoryService implements HistoryManagement {
    private final TaskService taskService;
    private final PersonService personService;
    private final HistoryService historyService;
    private final BpmnExtensionExtractor bpmnExtensionExtractor;
    private final ResourceBundleService resourceBundleService;

    @Override
    public PagedResponseData<HistoryProcessResponse> findProcessHistories(HistoryProcessRequest historyProcessRequest) throws Exception {
        Locale locale = AccessibleLocale.FA_IR.getLocale();//TODO get local from header
        String loggedInUserNationalCode = historyProcessRequest.getAssignee();
        GeneralPerson generalPerson = Objects.requireNonNull(AuthenticationUtils.getLoggedInUser()).getPerson();
        String userName = generalPerson.getUsername();
        HistoricTaskInstanceQuery historicTaskInstanceQuery = historyService.createHistoricTaskInstanceQuery().taskAssignee(loggedInUserNationalCode).or();
        int firstResult = (historyProcessRequest.getPageNo() - 1) * historyProcessRequest.getPageSize();
        int maxResult = Math.max((historyProcessRequest.getPageNo() * historyProcessRequest.getPageSize()) - 1, historyProcessRequest.getPageSize());
        setDate(historicTaskInstanceQuery, historyProcessRequest);
        if (historyProcessRequest.getActiveProcess() == null) {
            historicTaskInstanceQuery.processUnfinished();
            historicTaskInstanceQuery.processFinished();
        } else if (historyProcessRequest.getActiveProcess()) {
            historicTaskInstanceQuery.processUnfinished();
        } else {
            historicTaskInstanceQuery.processFinished();
        }
        List<HistoricTaskInstance> historicTaskInstanceList = historicTaskInstanceQuery.endOr().list();

        Set<String> processInstanceIds = historicTaskInstanceList.stream().map(HistoricTaskInstance::getProcessInstanceId).collect(Collectors.toSet());

        HistoricProcessInstanceQuery historicProcessInstanceQuery = historyService.createHistoricProcessInstanceQuery().or();
        if (!processInstanceIds.isEmpty()) {
            historicProcessInstanceQuery.processInstanceIds(processInstanceIds);
        } else if (historyProcessRequest.getFilters() == null) {
            return new PagedResponseData<>(historyProcessRequest.getPageNo(), historyProcessRequest.getPageSize(), 0L, new ArrayList<>());
        }
        long count = historicProcessInstanceQuery.endOr().count();
        List<HistoryProcessResponse> processHistories = createProcessHistories(historicProcessInstanceQuery.endOr().listPage(firstResult, maxResult));
        return new PagedResponseData<>(historyProcessRequest.getPageNo(), historyProcessRequest.getPageSize(), count, processHistories);
    }

    @Override
    public PagedResponseData<HistoryTaskResponse> findTaskHistories(HistoryTaskRequest historyTaskRequest) {
        HistoricTaskInstanceQuery historicTaskInstanceQuery = historyService.createHistoricTaskInstanceQuery().processInstanceId(historyTaskRequest.getProcessInstanceId());
        int firstResult = (historyTaskRequest.getPageNo() - 1) * historyTaskRequest.getPageSize();
        int maxResult = Math.max((historyTaskRequest.getPageNo() * historyTaskRequest.getPageSize()) - 1, historyTaskRequest.getPageSize());
        long count = historicTaskInstanceQuery.count();
        List<HistoricTaskInstance> historicTaskInstances = historicTaskInstanceQuery.listPage(firstResult, maxResult);
        List<HistoryTaskResponse> taskHistoryResponse = createTaskHistories(historicTaskInstances, historyTaskRequest);
        return new PagedResponseData<>(historyTaskRequest.getPageNo(), historyTaskRequest.getPageSize(), count, taskHistoryResponse);
    }

    private void setDate(HistoricTaskInstanceQuery historicTaskInstanceQuery, HistoryProcessRequest historyProcessRequest){
        Long fromDateMillis = historyProcessRequest.getFromDate();
        Long toDateMillis = historyProcessRequest.getToDate();

        Date startDate = (fromDateMillis != null) ? new Date(fromDateMillis) : null;
        Date toDate = (toDateMillis != null) ? new Date(toDateMillis) : null;

        Calendar calendar = Calendar.getInstance();

        if (startDate == null && toDate == null) {
            toDate = new Date();
            calendar.setTime(toDate);
            calendar.add(Calendar.MONTH, -1);
            startDate = calendar.getTime();
        } else if (startDate == null) {
            calendar.setTime(toDate);
            calendar.add(Calendar.MONTH, -1);
            startDate = calendar.getTime();
        } else if (toDate == null) {
            calendar.setTime(startDate);
            calendar.add(Calendar.MONTH, 1);
            toDate = calendar.getTime();
        }
        historicTaskInstanceQuery.startedAfter(startDate);
        historicTaskInstanceQuery.startedBefore(toDate);
    }

    private List<HistoryProcessResponse> createProcessHistories(List<HistoricProcessInstance> historicProcessInstances) throws JsonProcessingException {
        Locale locale = AccessibleLocale.FA_IR.getLocale();//TODO get local from header
        List<HistoryProcessResponse> historyProcessResponseList = new ArrayList<>();
        for (HistoricProcessInstance instance : historicProcessInstances) {
            HistoryProcessResponse response = new HistoryProcessResponse();
            response.setId(instance.getId());
            response.setRootProcessInstanceId(instance.getRootProcessInstanceId());
            response.setProcessName(resourceBundleService.get(locale,instance.getProcessDefinitionName()).orElse(instance.getProcessDefinitionName()));
            response.setState(resourceBundleService.get(locale, getStateBundleKey(instance.getState(), PROCESS)).orElse(instance.getState()));
            response.setDurationInMillis(instance.getDurationInMillis());
            response.setStartTime(Optional.ofNullable(instance.getStartTime()).map(Date::getTime).orElse(null));
            response.setEndTime(Optional.ofNullable(instance.getEndTime()).map(Date::getTime).orElse(null));
            response.setRemovalTime(Optional.ofNullable(instance.getRemovalTime()).map(Date::getTime).orElse(null));
            response.setData(getProcessVariables(instance.getId()));
            historyProcessResponseList.add(response);
        }
        return historyProcessResponseList;
    }

    private static String getStateBundleKey(String state, String type) {
        if (StringUtils.isEmpty(state)) {
            return null;
        }
        if (PROCESS.equalsIgnoreCase(type)) {
            state = PROCESS_STATE + state.toLowerCase();
        } else if (TASK.equalsIgnoreCase(type)) {
            state = TASK_STATE + state.toLowerCase();
        }
        return state;
    }

    public GeneralRealPersonEntity findPerson(String nationalCode) {
        if (nationalCode == null) {
            return null;
        }
        return personService.findPersonByNationalCode(nationalCode);
    }

    private List<HistoryTaskResponse> createTaskHistories(List<HistoricTaskInstance> historicTaskInstances, HistoryTaskRequest historyTaskRequest) {
        Locale locale = AccessibleLocale.FA_IR.getLocale();//TODO get local from header
        List<HistoryTaskResponse> responseList = new ArrayList<>();
        for (HistoricTaskInstance instance : historicTaskInstances) {
            if (instance.getPriority() < 0) {
                continue;
            }
            HistoryTaskResponse response = new HistoryTaskResponse();
            response.setId(instance.getId());
            Assignment assignment = new Assignment();
            assignment.setUsername(instance.getAssignee());
            if (historyTaskRequest.isIncludePersonInfo()) {
                GeneralRealPersonEntity person = findPerson(instance.getAssignee());
                if (person != null) {
                    assignment.setFirstName(person.getFirstName());
                    assignment.setLastName(person.getLastName());
                    assignment.setFirstNameEnglish(person.getFirstNameEnglish());
                    assignment.setLastNameEnglish(person.getLastNameEnglish());
                    assignment.setNationalCode(person.getNationalCode());
                }
            }
            response.setAssignment(List.of(assignment));
            response.setTaskName(resourceBundleService.get(locale,instance.getName()).orElse(instance.getName()));
            response.setStartTime(Optional.ofNullable(instance.getStartTime()).map(Date::getTime).orElse(null));
            response.setEndTime(Optional.ofNullable(instance.getEndTime()).map(Date::getTime).orElse(null));
            response.setDeleteReason(instance.getDeleteReason());
            response.setState(resourceBundleService.get(AccessibleLocale.FA_IR.getLocale(), getStateBundleKey(instance.getDeleteReason(), TASK)).orElse(null));
            response.setData(getTaskVariables(instance.getRootProcessInstanceId(), instance));
            ProcessInstanceInfo processInstance = new ProcessInstanceInfo();
            processInstance.setProcessInstanceId(instance.getProcessInstanceId());
            response.setProcessInstance(processInstance);
            responseList.add(response);
        }
        return responseList;
    }

    private Map<String, Object> getProcessVariables(String processInstanceId){
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> businessData = new HashMap<>();
        Map<String, String> extensionProperties = bpmnExtensionExtractor.getExtensionProperties(processInstanceId, Collaboration.class);

        if (extensionProperties.containsKey(PROCESS_EXTENSION)) {
            processVariablesFromExtensions(data, businessData, extensionProperties.get(PROCESS_EXTENSION), processInstanceId);
        }
        data.put(BUSINESS_DATA, businessData);
        return data;
    }

    private void processVariablesFromExtensions(Map<String, Object> data, Map<String, Object> businessData, String extensionProperty, String processInstanceId) {
        List<HistoricVariableInstance> variables = historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstanceId).list();
        Set<String> extensions = new HashSet<>(Arrays.asList(extensionProperty.split(",")));
        for (HistoricVariableInstance variable : variables) {
            if (extensions.contains(variable.getName())) {
                addVariableToMap(variable, data, businessData);
            }
        }
    }

    private void addVariableToMap(HistoricVariableInstance variable, Map<String, Object> data, Map<String, Object> businessData) {
        String key = variable.getName();
        if (key.startsWith(BUSINESS_DATA)) {
            key = key.replace(BUSINESS_DATA + "_", "");
            businessData.put(key, variable.getValue());
        } else {
            data.put(variable.getName(), variable.getValue());
        }
    }

    private Map<String, Object> getTaskVariables(String processInstanceId, HistoricTaskInstance historicTaskInstance) {
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> businessData = new HashMap<>();
        Map<String, String> extensionProperties = bpmnExtensionExtractor.getExtensionProperties(processInstanceId, Collaboration.class);
        if (extensionProperties.containsKey(TASK_EXTENSION)) {
            Set<String> searchSet = new HashSet<>(Arrays.asList(extensionProperties.get(TASK_EXTENSION).split(",")));
            HistoryTaskInfo historyTaskInfo = new HistoryTaskInfo(searchSet);
            addHistoricTaskVariables(data, businessData, historicTaskInstance, historyTaskInfo);
        }
        data.put(BUSINESS_DATA, businessData);
        return data;
    }

    private void addHistoricTaskVariables(Map<String, Object> data, Map<String, Object> businessData, HistoricTaskInstance historicTaskInstance, HistoryTaskInfo historyTaskInfo) {
        Task task = taskService.createTaskQuery().taskId(historicTaskInstance.getId()).singleResult();
        if (task == null) {
            addVariablesFromHistory(data, businessData, historyTaskInfo, historicTaskInstance.getExecutionId());
            addVariablesFromProcessHistory(data, businessData, historyTaskInfo, historicTaskInstance.getProcessInstanceId());
        } else {
            addVariablesFromTask(data, businessData, historyTaskInfo, historicTaskInstance.getId());
        }
    }

    private void addVariablesFromHistory(Map<String, Object> data, Map<String, Object> businessData, HistoryTaskInfo historyTaskInfo, String executionId) {
        List<HistoricVariableInstance> variables = historyService.createHistoricVariableInstanceQuery().executionIdIn(executionId).list();
        addVariables(data, businessData, historyTaskInfo, variables);
    }

    private void addVariablesFromProcessHistory(Map<String, Object> data, Map<String, Object> businessData, HistoryTaskInfo historyTaskInfo, String processInstanceId) {
        List<HistoricVariableInstance> variables = historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstanceId).list();
        addVariables(data, businessData, historyTaskInfo, variables);
    }

    private void addVariables(Map<String, Object> data, Map<String, Object> businessData, HistoryTaskInfo historyTaskInfo, List<HistoricVariableInstance> variables) {
        for (HistoricVariableInstance variable : variables) {
            if (historyTaskInfo.getSearch().contains(variable.getName())) {
                addVariableToMap(variable, data, businessData);
            }
        }
    }

    private void addVariablesFromTask(Map<String, Object> data, Map<String, Object> businessData, HistoryTaskInfo historyTaskInfo, String taskId) {
        Map<String, Object> variables = taskService.getVariables(taskId);
        for (Map.Entry<String, Object> variable : variables.entrySet()) {
            if (historyTaskInfo.getSearch().contains(variable.getKey())) {
                addVariableToMap(variable.getKey(), variable.getValue(), data, businessData);
            }
        }
    }

    private void addVariableToMap(String key, Object value, Map<String, Object> data, Map<String, Object> businessData) {
        if (key.startsWith(BUSINESS_DATA)) {
            key = key.replace(BUSINESS_DATA + "_", "");
            businessData.put(key, value);
        } else {
            data.put(key, value);
        }
    }
}

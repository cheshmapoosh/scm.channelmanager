package ir.daneshrefah.scm.process.input;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.annotation.JavaService;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.process.service.dto.history.HistoryProcessRequest;
import ir.daneshrefah.scm.process.service.dto.history.HistoryProcessResponse;
import ir.daneshrefah.scm.process.service.dto.history.HistoryTaskRequest;
import ir.daneshrefah.scm.process.service.dto.history.HistoryTaskResponse;
import ir.daneshrefah.scm.process.service.history.HistoryManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static ir.daneshrefah.scm.common.constant.OperationCode.SVC_PROCESS_HISTORY_LIST;
import static ir.daneshrefah.scm.common.constant.OperationCode.SVC_PROCESS_TASK_DETAIL_HISTORY_LIST;

@Service
public class HistoryManagementService extends AbstractJavaService {

    @Autowired
    private HistoryManagement historyManagement;

    @JavaService(operationCode = SVC_PROCESS_HISTORY_LIST)
    public PagedResponseData<HistoryProcessResponse> findProcessHistories(HistoryProcessRequest historyProcessRequest) throws Exception {
        return historyManagement.findProcessHistories(historyProcessRequest);
    }

    public HistoryManagementService(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper) {
        super(producerTemplate, objectMapper);
    }

    @JavaService(operationCode = SVC_PROCESS_TASK_DETAIL_HISTORY_LIST)
    public PagedResponseData<HistoryTaskResponse> findTaskHistories(HistoryTaskRequest historyTaskRequest) throws JsonProcessingException {
        return historyManagement.findTaskHistories(historyTaskRequest);
    }
}
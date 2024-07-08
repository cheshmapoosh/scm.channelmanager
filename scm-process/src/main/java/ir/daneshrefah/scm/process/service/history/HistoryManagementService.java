package ir.daneshrefah.scm.process.service.history;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.plugin.api.annotation.JavaService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.process.service.dto.history.HistoryProcessRequest;
import ir.daneshrefah.scm.process.service.dto.history.HistoryProcessResponse;
import ir.daneshrefah.scm.process.service.dto.history.HistoryTaskRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HistoryManagementService extends AbstractJavaService {

    @Autowired
    private HistoryManagement historyManagement;

    public HistoryManagementService(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper) {
        super(producerTemplate, objectMapper);
    }

    @JavaService
    public PagedResponseData<HistoryProcessResponse> findProcessHistories(HistoryProcessRequest historyProcessRequest) throws Exception {
        return historyManagement.findProcessHistories(historyProcessRequest);
    }

    @JavaService
    public PagedResponseData findTaskHistories(HistoryTaskRequest historyTaskRequest) throws JsonProcessingException {
        return historyManagement.findTaskHistories(historyTaskRequest);
    }
}
package ir.daneshrefah.scm.process.service.history;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.process.model.filter.ProcessFilter;
import ir.daneshrefah.scm.process.model.request.HistoryRequest;
import ir.daneshrefah.scm.process.model.response.ProcessHistoryResponse;
import ir.daneshrefah.scm.process.model.response.TaskHistoryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HistoryManagementService extends AbstractJavaService {

    @Autowired
    private HistoryManagement historyManagement;

    public HistoryManagementService(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper) {
        super(producerTemplate, objectMapper);
    }

    public List<TaskHistoryResponse> findTaskHistories(HistoryRequest historyRequest) throws JsonProcessingException {
        return historyManagement.findTaskHistories(historyRequest);
    }

    public List<ProcessHistoryResponse> findProcessHistories(ProcessFilter processFilter) throws JsonProcessingException {
        return historyManagement.findProcessHistories(processFilter);
    }
}
package ir.daneshrefah.scm.plugin.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.logging.model.LogTraceDetailResponse;
import ir.daneshrefah.scm.logging.model.LogTraceRequest;
import ir.daneshrefah.scm.logging.model.LogTraceResponse;
import ir.daneshrefah.scm.logging.service.LogService;
import ir.daneshrefah.scm.plugin.api.annotation.JavaService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import org.springframework.stereotype.Service;

@Service
public class TransactionLogManagementService extends AbstractJavaService {
    private final LogService logService;

    public TransactionLogManagementService(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper, LogService logService) {
        super(producerTemplate, objectMapper);
        this.logService = logService;
    }

    @JavaService
    public PagedResponseData<LogTraceResponse> findAll(LogTraceRequest request) {
        return logService.findAll(request);
    }

    @JavaService
    public LogTraceDetailResponse findById(Long id) {
        return logService.findById(id);
    }
}


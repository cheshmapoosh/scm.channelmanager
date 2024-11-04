package ir.daneshrefah.scm.plugin.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.logging.model.TransactionLogDetailResponse;
import ir.daneshrefah.scm.logging.model.TransactionLogRequest;
import ir.daneshrefah.scm.logging.model.TransactionLogResponse;
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
    public PagedResponseData<TransactionLogResponse> findAll(TransactionLogRequest request) {
        return logService.findAll(request);
    }

    @JavaService
    public TransactionLogDetailResponse findById(Long id) {
        return logService.findById(id);
    }
}


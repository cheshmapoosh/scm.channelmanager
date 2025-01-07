package ir.daneshrefah.scm.plugin.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.logging.model.LogTraceDetailResponse;
import ir.daneshrefah.scm.logging.model.LogTraceRequest;
import ir.daneshrefah.scm.logging.model.LogTraceResponse;
import ir.daneshrefah.scm.logging.service.LogService;
import ir.daneshrefah.scm.common.annotation.JavaService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import org.springframework.stereotype.Service;

import static ir.daneshrefah.scm.common.constant.ServiceCode.SCV_LOG_FIND_BY_ID;
import static ir.daneshrefah.scm.common.constant.ServiceCode.SCV_LOG_LIST;

@Service
public class TransactionLogManagementService extends AbstractJavaService {
    private final LogService logService;

    public TransactionLogManagementService(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper, LogService logService) {
        super(producerTemplate, objectMapper);
        this.logService = logService;
    }

    @JavaService(serviceCode = SCV_LOG_LIST)
    public PagedResponseData<LogTraceResponse> findAll(LogTraceRequest request) {
        return logService.findAll(request);
    }

    @JavaService(serviceCode = SCV_LOG_FIND_BY_ID)
    public LogTraceDetailResponse findById(Long id) {
        return logService.findById(id);
    }
}


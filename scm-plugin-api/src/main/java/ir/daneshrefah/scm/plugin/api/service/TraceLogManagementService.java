package ir.daneshrefah.scm.plugin.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;


import ir.daneshrefah.scm.common.annotation.JavaService;
import ir.daneshrefah.scm.common.log.model.LogTraceFindByIdRequest;
import ir.daneshrefah.scm.common.log.model.LogTracePayloadResponse;
import ir.daneshrefah.scm.common.log.model.LogTraceRequest;
import ir.daneshrefah.scm.common.log.model.LogTraceResponse;
import ir.daneshrefah.scm.common.log.service.LogService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import org.springframework.stereotype.Service;

import static ir.daneshrefah.scm.common.constant.OperationCode.SCV_LOG_FIND_BY_ID;
import static ir.daneshrefah.scm.common.constant.OperationCode.SCV_LOG_LIST;


@Service
public class TraceLogManagementService extends AbstractJavaService {
    private final LogService logService;

    public TraceLogManagementService(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper, LogService logService) {
        super(producerTemplate, objectMapper);
        this.logService = logService;
    }

    @JavaService(operationCode = SCV_LOG_LIST)
    public PagedResponseData<LogTraceResponse> findAll(LogTraceRequest request) {
        return logService.findAll(request);
    }

    @JavaService(operationCode = SCV_LOG_FIND_BY_ID)//TODO change the service name
    public LogTracePayloadResponse getPayload(LogTraceFindByIdRequest request) {
        return logService.getPayload(request);
    }

}


package ir.daneshrefah.scm.plugin.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.annotation.JavaService;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.log.model.TransactionLogRequest;
import ir.daneshrefah.scm.common.log.model.TransactionLogResponse;
import ir.daneshrefah.scm.common.log.model.TransactionPayloadRequest;
import ir.daneshrefah.scm.common.log.service.TransactionLogService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import org.springframework.stereotype.Service;

import static ir.daneshrefah.scm.common.constant.OperationCode.SCV_LOG_TRANSACTION_DETAIL;
import static ir.daneshrefah.scm.common.constant.OperationCode.SCV_LOG_TRANSACTION_LIST;

@Service
public class TransactionLogManagementService extends AbstractJavaService {

    private final TransactionLogService transactionLogService;

    public TransactionLogManagementService(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper, TransactionLogService transactionLogService) {
        super(producerTemplate, objectMapper);
        this.transactionLogService = transactionLogService;
    }

    @JavaService(operationCode = SCV_LOG_TRANSACTION_LIST)
    public PagedResponseData<TransactionLogResponse> findAll(TransactionLogRequest request) {
        return transactionLogService.findAll(request);
    }

    @JavaService(operationCode = SCV_LOG_TRANSACTION_DETAIL)
    public TransactionLogResponse getDetails(TransactionPayloadRequest request) {
        return transactionLogService.getDetails(request);
    }
}


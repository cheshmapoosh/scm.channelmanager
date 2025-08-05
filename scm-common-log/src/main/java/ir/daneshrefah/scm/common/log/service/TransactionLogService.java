package ir.daneshrefah.scm.common.log.service;

import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.log.entity.transaction.TransactionLogEntity;
import ir.daneshrefah.scm.common.log.model.TransactionLogRequest;
import ir.daneshrefah.scm.common.log.model.TransactionLogResponse;
import ir.daneshrefah.scm.common.log.model.TransactionPayloadRequest;

import java.util.List;

public interface TransactionLogService {
    void saveAll(List<TransactionLogEntity> transactionLogs);

    PagedResponseData<TransactionLogResponse> findAll(TransactionLogRequest request);

    TransactionLogResponse getDetails(TransactionPayloadRequest request);
}

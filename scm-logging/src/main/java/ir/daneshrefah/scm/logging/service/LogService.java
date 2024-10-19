package ir.daneshrefah.scm.logging.service;

import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.logging.model.TransactionLogDetailResponse;
import ir.daneshrefah.scm.logging.model.TransactionLogRequest;
import ir.daneshrefah.scm.logging.model.TransactionLogResponse;

public interface LogService {

    void save(String msg);

    PagedResponseData<TransactionLogResponse> findAll(TransactionLogRequest request);

    TransactionLogDetailResponse findById(Long id);
}

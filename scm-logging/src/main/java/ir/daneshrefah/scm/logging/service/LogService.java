package ir.daneshrefah.scm.logging.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.logging.model.LogTraceDetailResponse;
import ir.daneshrefah.scm.logging.model.LogTraceRequest;
import ir.daneshrefah.scm.logging.model.LogTraceResponse;

public interface LogService {

    void save(String msg) throws Exception;

    PagedResponseData<LogTraceResponse> findAll(LogTraceRequest request);

    LogTraceDetailResponse findById(Long id);
}

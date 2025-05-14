package ir.daneshrefah.scm.logging.service;

import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.model.logging.*;

public interface LogService {

    void save(String msg);

    PagedResponseData<LogTraceResponse> findAll(LogTraceRequest request);

    LogTracePayloadResponse getPayload(LogTraceFindByIdRequest request);
}

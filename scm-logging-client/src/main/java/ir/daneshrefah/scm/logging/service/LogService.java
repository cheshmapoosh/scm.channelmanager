package ir.daneshrefah.scm.logging.service;

import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.model.logging.LogTraceFindByIdRequest;
import ir.daneshrefah.scm.common.model.logging.LogTracePayloadResponse;
import ir.daneshrefah.scm.common.model.logging.LogTraceRequest;
import ir.daneshrefah.scm.common.model.logging.LogTraceResponse;

public interface LogService {

    PagedResponseData<LogTraceResponse> findAll(LogTraceRequest request);

    LogTracePayloadResponse getPayload(LogTraceFindByIdRequest request);
}

package ir.daneshrefah.scm.common.log.service;

import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.log.entity.logging.LogTraceEntity;
import ir.daneshrefah.scm.common.log.model.LogTraceFindByIdRequest;
import ir.daneshrefah.scm.common.log.model.LogTracePayloadResponse;
import ir.daneshrefah.scm.common.log.model.LogTraceRequest;
import ir.daneshrefah.scm.common.log.model.LogTraceResponse;

import java.util.List;

public interface LogService {
    void saveAll(List<LogTraceEntity> logTraces);

    PagedResponseData<LogTraceResponse> findAll(LogTraceRequest request);

    LogTracePayloadResponse getPayload(LogTraceFindByIdRequest request);
}

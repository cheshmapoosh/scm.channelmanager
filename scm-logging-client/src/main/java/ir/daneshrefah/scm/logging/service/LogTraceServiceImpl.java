package ir.daneshrefah.scm.logging.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.data.entity.logging.LogTraceEntity;
import ir.daneshrefah.scm.common.data.repository.logging.LogTraceRepository;
import ir.daneshrefah.scm.common.data.repository.logging.TraceLogSpec;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.model.logging.LogTraceFindByIdRequest;
import ir.daneshrefah.scm.common.model.logging.LogTracePayloadResponse;
import ir.daneshrefah.scm.common.model.logging.LogTraceRequest;
import ir.daneshrefah.scm.common.model.logging.LogTraceResponse;
import ir.daneshrefah.scm.logging.mapper.LogTraceMapper;
import ir.daneshrefah.scm.logging.utils.PageableUtils;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogTraceServiceImpl implements LogService {

    private final LogTraceRepository logTraceRepository;

    @Override
    public PagedResponseData<LogTraceResponse> findAll(LogTraceRequest request) {
        request = Objects.nonNull(request) ? request : new LogTraceRequest();
        Pageable pageable = PageableUtils.getPageable(request);
        Page<LogTraceEntity> entities = logTraceRepository.findAll(TraceLogSpec.toSpecification(request), pageable);
        LogTraceMapper instance = LogTraceMapper.INSTANCE;
        List<LogTraceResponse> logTraceResponseList = instance.toModelList(entities.getContent());
        return new PagedResponseData<>(request.getPageNo(), request.getPageSize(), entities.getTotalElements(), logTraceResponseList);
    }

    @Override
    public LogTracePayloadResponse getPayload(LogTraceFindByIdRequest request) {
        try {
            ValidationUtils.checkBlankString(request.getSpanId(), () -> new MissingRequiredInputException("spainId"));
            ValidationUtils.checkBlankString(request.getTraceId(), () -> new MissingRequiredInputException("traceId"));
            String payload = logTraceRepository.findAggregatedPayload(request.getSpanId(), request.getTraceId());
            if (payload == null) {
                return new LogTracePayloadResponse();
            }
            return new LogTracePayloadResponse(payload);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read CLOB result", e);
        }
    }
}

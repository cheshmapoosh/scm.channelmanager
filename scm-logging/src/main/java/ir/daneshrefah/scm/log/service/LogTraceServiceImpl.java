package ir.daneshrefah.scm.common.log.service;

import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.log.entity.logging.LogTraceEntity;
import ir.daneshrefah.scm.common.log.mapper.LogTraceMapper;
import ir.daneshrefah.scm.common.log.model.LogTraceFindByIdRequest;
import ir.daneshrefah.scm.common.log.model.LogTracePayloadResponse;
import ir.daneshrefah.scm.common.log.model.LogTraceRequest;
import ir.daneshrefah.scm.common.log.model.LogTraceResponse;
import ir.daneshrefah.scm.common.log.repository.logging.LogTraceRepository;
import ir.daneshrefah.scm.common.log.repository.logging.TraceLogSpec;
import ir.daneshrefah.scm.common.log.utils.PageableUtils;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogTraceServiceImpl implements LogService {

    private final LogTraceRepository logTraceRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveAll(List<LogTraceEntity> logTraces) {
        logTraceRepository.saveAll(logTraces);
    }

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

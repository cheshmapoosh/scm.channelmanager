package ir.daneshrefah.scm.logging.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.logging.entity.LogPrimaryKey;
import ir.daneshrefah.scm.logging.entity.LogTraceEntity;
import ir.daneshrefah.scm.logging.mapper.LogTraceMapper;
import ir.daneshrefah.scm.logging.model.*;
import ir.daneshrefah.scm.logging.repository.LogTraceRepository;
import ir.daneshrefah.scm.logging.utils.PageableUtils;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogTraceServiceImpl implements LogService {

    private final LogTraceRepository logTraceRepository;
    private final ConverterService converterService;
    private final ObjectMapper objectMapper;

    public void save(String msg) {
        try {
            LogMessage logMessage = converterService.convertToLogMessage(msg);
            LogTraceEntity logTraceEntity = converterService.convertToLogTraceEntity(logMessage);
            logTraceEntity.setPayload(objectMapper.writeValueAsString(logMessage));
            logTraceRepository.save(logTraceEntity);
        } catch (Exception e) {
            log.error("Failed to save message: {} due to error: {}", msg, e.getMessage(), e);
        }
    }

    @Override
    public PagedResponseData<LogTraceResponse> findAll(LogTraceRequest request) {
        request = Objects.nonNull(request) ? request : new LogTraceRequest();
        Pageable pageable = PageableUtils.getPageable(request);
        Page<LogTraceResponse> entities = logTraceRepository.findAll(
                request.getChannelCode(),
                request.getTerminalCode(),
                request.getClientId(),
                request.getCorrelationId(),
                request.getClientCorrelationId(),
                request.getMessageId(),
                request.getStatusCode(),
                request.getNickname(),
                request.getUsername(),
                request.getDelegatorUsername(),
                request.getEndPoint(),
                request.getAmount(),
                request.getAccountNo(),
                request.getCardNo(),
                request.getStartTime(),
                request.getEndTime(),
                pageable);
        return new PagedResponseData<>(request.getPageNo(), request.getPageSize(), entities.getTotalElements(), entities.getContent());
    }

    @Override
    public LogTraceDetailResponse findById(LogTraceFindByIdRequest request) {
        LogTraceMapper instance = LogTraceMapper.INSTANCE;
        ValidationUtils.checkBlankString(request.getSpanId(), () -> new MissingRequiredInputException("spainId"));
        ValidationUtils.checkBlankString(request.getTraceId(), () -> new MissingRequiredInputException("traceId"));
        LogPrimaryKey logPrimaryKey = new LogPrimaryKey(request.getSpanId(), request.getTraceId());
        LogTraceEntity logTraceEntity = logTraceRepository.findById(logPrimaryKey).orElseThrow(() -> new NoMatchRecordFoundException("spanId&TraceID"));
        return instance.toModel(logTraceEntity);
    }
}

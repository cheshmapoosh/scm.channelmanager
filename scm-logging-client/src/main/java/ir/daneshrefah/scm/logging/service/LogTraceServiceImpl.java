package ir.daneshrefah.scm.logging.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.logging.entity.LogTraceEntity;
import ir.daneshrefah.scm.logging.mapper.LogTraceMapper;
import ir.daneshrefah.scm.logging.model.*;
import ir.daneshrefah.scm.logging.repository.LogTraceRepository;
import ir.daneshrefah.scm.logging.utils.PageableUtils;
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

    public void save(String msg) throws Exception {
        LogMessage logMessage = converterService.convertToLogMessage(msg);
        LogTraceEntity logTraceEntity = converterService.convertToLogTraceEntity(logMessage);
        logTraceEntity.setPayload(objectMapper.writeValueAsString(logMessage));
        logTraceRepository.save(logTraceEntity);
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
    public LogTraceDetailResponse findById(Long id) {
        LogTraceMapper instance = LogTraceMapper.INSTANCE;
        LogTraceEntity logTraceEntity = logTraceRepository.findById(id).orElseThrow(() -> new NoMatchRecordFoundException("id"));
        return instance.toModel(logTraceEntity);
    }
}

package ir.daneshrefah.scm.logging.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.logging.entity.LogTraceEntity;
import ir.daneshrefah.scm.logging.mapper.TransactionLogMapper;
import ir.daneshrefah.scm.logging.model.LogMessage;
import ir.daneshrefah.scm.logging.model.TransactionLogDetailResponse;
import ir.daneshrefah.scm.logging.model.TransactionLogRequest;
import ir.daneshrefah.scm.logging.model.TransactionLogResponse;
import ir.daneshrefah.scm.logging.repository.TransactionLogRepository;
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
public class TransactionLogServiceImpl implements LogService {
    private final TransactionLogRepository transactionLogRepository;
    private final ConverterService converterService;

    private final ObjectMapper objectMapper;

    public void save(String msg) {
        LogTraceEntity logTraceEntity;
        try {
            LogMessage logMessage = converterService.convertToLogMessage(msg);
            logTraceEntity = converterService.convertToTransactionLogEntity(logMessage);
            logTraceEntity.setPayload(objectMapper.writeValueAsString(logMessage));
            transactionLogRepository.save(logTraceEntity);
        } catch (Exception e) {
            log.error("Failed to save message: {} due to error: {}", msg, e.getMessage(), e);
        }
    }

    @Override
    public PagedResponseData<TransactionLogResponse> findAll(TransactionLogRequest request) {
        request = Objects.nonNull(request) ? request : new TransactionLogRequest();
        Pageable pageable = PageableUtils.getPageable(request);
        Page<TransactionLogResponse> entities = transactionLogRepository.findAll(
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
    public TransactionLogDetailResponse findById(Long id) {
        TransactionLogMapper instance = TransactionLogMapper.INSTANCE;
        LogTraceEntity logTraceEntity = transactionLogRepository.findById(id).orElseThrow(() -> new NoMatchRecordFoundException("id"));
        return instance.toModel(logTraceEntity);
    }
}

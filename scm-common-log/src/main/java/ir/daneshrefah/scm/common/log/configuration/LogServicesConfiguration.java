package ir.daneshrefah.scm.common.log.configuration;

import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.log.entity.logging.LogTraceEntity;
import ir.daneshrefah.scm.common.log.entity.transaction.TransactionLogEntity;
import ir.daneshrefah.scm.common.log.model.*;
import ir.daneshrefah.scm.common.log.service.LogService;
import ir.daneshrefah.scm.common.log.service.TransactionLogService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class LogServicesConfiguration {

    @Bean
    @ConditionalOnMissingBean(LogService.class)
    public LogService disabledLogService() {
        return new LogService() {
            @Override
            public void saveAll(List<LogTraceEntity> logTraces) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public PagedResponseData<LogTraceResponse> findAll(LogTraceRequest request) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public LogTracePayloadResponse getPayload(LogTraceFindByIdRequest request) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(TransactionLogService.class)
    public TransactionLogService disabledTransactionLogService() {
        return new TransactionLogService() {
            @Override
            public void saveAll(List<TransactionLogEntity> transactionLogs) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public PagedResponseData<TransactionLogResponse> findAll(TransactionLogRequest request) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public TransactionLogResponse getDetails(TransactionPayloadRequest request) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }

}

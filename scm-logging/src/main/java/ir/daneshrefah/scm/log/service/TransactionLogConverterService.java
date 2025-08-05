package ir.daneshrefah.scm.log.service;

import ir.daneshrefah.scm.common.log.entity.transaction.TransactionLogEntity;
import ir.daneshrefah.scm.common.log.entity.transaction.TransactionLogId;
import ir.daneshrefah.scm.common.log.service.TransactionLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionLogConverterService implements ConverterService {

    private final TransactionLogService transactionLogService;

    @Override
    public void convertAndPersist(String message) {
        List<TransactionLogEntity> entities = convert(message);
        transactionLogService.saveAll(entities);
    }

    private List<TransactionLogEntity> convert(String message) {
        TransactionLogEntity transactionLogEntity = new TransactionLogEntity();
//        transactionLogEntity.setLogTime(LocalDateTime.now());
//        transactionLogEntity.setMessageSequenceId(UUID.randomUUID().toString());
//        TransactionLogId transactionLogId = new TransactionLogId();
//        transactionLogId.setArchiveNo(9);
//        transactionLogEntity.setId(transactionLogId);
        return List.of(transactionLogEntity);
    }
}

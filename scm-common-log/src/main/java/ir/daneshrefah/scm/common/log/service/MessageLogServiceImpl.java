package ir.daneshrefah.scm.common.log.service;


import ir.daneshrefah.scm.common.log.configuration.LogConditions;
import ir.daneshrefah.scm.common.log.entity.message.MessageLogEntity;
import ir.daneshrefah.scm.common.log.repository.message.MessageLogRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Conditional(LogConditions.MessageLogCondition.class)
public class MessageLogServiceImpl implements MessageLogService {

    private final MessageLogRepository messageLogRepository;

    @PostConstruct
    public void init() {
        log.info(">>> MessageLogServiceImpl successfully initialized");
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveAll(List<MessageLogEntity> messageLogs) {
                messageLogRepository.saveAll(messageLogs);
    }

   
}

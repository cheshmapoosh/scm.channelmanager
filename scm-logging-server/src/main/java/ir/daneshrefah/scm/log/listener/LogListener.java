package ir.daneshrefah.scm.log.listener;

import ir.daneshrefah.scm.log.service.TransactionLogService;
import jakarta.jms.Session;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class LogListener {

    private final TransactionLogService transactionLogService;

    @SneakyThrows
    @JmsListener(destination = "${scm.mq.destination}",
            concurrency = "${scm.mq.concurrency}")
    public void receiveMessage(String msg, Session session) {
        try {
            transactionLogService.save(msg);
        } catch (Exception e) {
            log.error(transactionLogService.createExceptionLog(e, msg));
        }
        session.commit();
    }
}
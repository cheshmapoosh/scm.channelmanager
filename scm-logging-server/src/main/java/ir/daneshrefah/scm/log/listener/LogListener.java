package ir.daneshrefah.scm.log.listener;

import ir.daneshrefah.scm.log.service.TransactionLogService;
import lombok.AllArgsConstructor;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class LogListener {

    private final TransactionLogService transactionLogService;

    @JmsListener(destination = "${scm.mq.destination}", concurrency = "${scm.mq.concurrency}")
    public void receiveMessage(String msg) {
        transactionLogService.save(msg);
    }
}
package ir.daneshrefah.scm.log.listener;

import ir.daneshrefah.scm.logging.service.LogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "scm.log.listener.enabled", havingValue = "true", matchIfMissing = true)
public class LogListener {

    private final LogService logService;

    @JmsListener(destination = "${scm.mq.destination}", concurrency = "${scm.mq.concurrency}")
    public void receiveMessage(String msg) {
        try {
            logService.save(msg);
        } catch (Exception e) {
            log.error("Failed to save message: {} due to error: {}", msg, e.getMessage(), e);
        }
    }
}
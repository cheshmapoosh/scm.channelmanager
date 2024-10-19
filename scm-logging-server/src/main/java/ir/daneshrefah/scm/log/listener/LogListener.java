package ir.daneshrefah.scm.log.listener;

import ir.daneshrefah.scm.logging.service.LogService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
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

    @SneakyThrows
    @JmsListener(destination = "${scm.mq.destination}", concurrency = "${scm.mq.concurrency}")
    public void receiveMessage(String msg) {
        logService.save(msg);
    }
}
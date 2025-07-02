package ir.daneshrefah.scm.log.service;

import ir.daneshrefah.scm.log.config.LogJmsConfigProperties;
import ir.daneshrefah.scm.mq.jms.message.JakartaMessage;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class MQConsumer {

    private final JmsTemplate logJmsTemplate;
    private final LogService logService;
    private final LogJmsConfigProperties properties;

    @Scheduled(fixedRate = 5000)
    @Async("logSchedulerThreadPool")
    public void consumeMessages() {
        while (true) {
            try {
                Message message = logJmsTemplate.receive(properties.getDestination());
                if (message instanceof JakartaMessage jakartaMessage) {
                    String msg = jakartaMessage.getBody(String.class);
                    logService.save(msg);
                } else {
                    log.error("Message body is Not text message {}", message);
                }
            } catch (JMSException e) {
                log.error("Failed to receive or process message: {} ", e.getMessage());
            }
        }
    }
}
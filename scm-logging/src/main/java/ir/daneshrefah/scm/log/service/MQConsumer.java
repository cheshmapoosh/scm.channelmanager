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
    private final LogJmsConfigProperties properties;
    private final MessageProcessingService messageProcessingService;

    @Scheduled(fixedRateString = "${scm.log.logSchedulerThreadPool.fixedRate:5000}")
    @Async("logSchedulerThreadPool")
    public void consumeMessages() {
        while (true) {
            String messageBody = null;
            try {
                Message message = receiveMessage();
                if (message == null) {
                    log.error("No message received from queue [{}] ", properties.getDestination());
                    return;
                }
                messageBody = extractMessageBody(message);
                messageProcessingService.processMessage(messageBody);
            } catch (JMSException e) {
                log.error("Failed to receive message from queue [{}]: {}",
                        properties.getDestination(), e.getMessage(), e);
            } catch (Exception e) {
                log.error("Failed to process message. Message body: [{}]. Error: {}",
                        messageBody != null ? messageBody : "null", e.getMessage(), e);
            }
        }
    }

    private Message receiveMessage() {
        return logJmsTemplate.receive(properties.getDestination());
    }

    private String extractMessageBody(Message message) throws JMSException {
        if (message instanceof JakartaMessage jakartaMessage) {
            return jakartaMessage.getBody(String.class);
        } else {
            log.error("Message body is Not text message {}", message);
            return null;
        }
    }

}
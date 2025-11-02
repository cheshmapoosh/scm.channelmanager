package ir.daneshrefah.scm.log.service;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.JmsListener;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class MQConsumer {

    private final MessageProcessingService messageProcessingService;

    @JmsListener(
            destination = "${spring.jms.template.default-destination:SCM2LOG}"
    )
    public void consumeMessages(Message message) {
        try {
            if (message == null) {
                log.warn("Received null message, skipping.");
                return;
            }
            String messageBody = extractMessageBody(message);
            if (messageBody != null) {
                messageProcessingService.processMessage(messageBody);
            } else {
                log.warn("Message body is null, skipping processing.");
            }
        } catch (JMSException e) {
            log.error("Failed to read message body: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error while processing message: {}", e.getMessage(), e);
        }
    }

    private String extractMessageBody(Message message) throws JMSException {
        try {
            return message.getBody(String.class);
        } catch (Exception e) {
            log.error("Failed to extract message body: {}", e.getMessage(), e);
            throw e;
        }
    }
}
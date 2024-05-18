package ir.daneshrefah.scm.notification.client.service.provider;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.common.model.notification.NotificationMessage;
import ir.daneshrefah.scm.common.model.notification.constants.NotificationMedia;
import jakarta.jms.TextMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-18
 */
@RequiredArgsConstructor
@Component
public class SMSNotificationMessageProvider implements NotificationMessageProvider {

    @Qualifier("smsJmsTemplate")
    private final JmsTemplate smsJmsTemplate;

    @Override
    public void send(NotificationMessage notificationMessage) {
        ObjectNode jsonMessage = JsonNodeFactory.instance.objectNode();
        jsonMessage.put("messageType", notificationMessage.getRequest().getTemplate().name());
//        jsonMessage.put("messageId", notificationMessage.getMessageId());
//        jsonMessage.put("name", null);
//        jsonMessage.put("date", null);
//        jsonMessage.put("time", null);
//        jsonMessage.put("mobileNumber", null);
//        jsonMessage.put("message", null);
//        jsonMessage.put("channelCode", null);
        smsJmsTemplate.send(session -> {
            TextMessage textMessage = session.createTextMessage();
            textMessage.setText(jsonMessage.asText());
            textMessage.setJMSCorrelationID(java.util.UUID.randomUUID().toString());
            return textMessage;
        });
    }

    @Override
    public boolean supports(NotificationMessage notificationMessage) {
        return Objects.nonNull(notificationMessage) && Objects.nonNull(notificationMessage.getRequest()) &&
                NotificationMedia.SMS.equals(notificationMessage.getRequest().getMedia());
    }

}

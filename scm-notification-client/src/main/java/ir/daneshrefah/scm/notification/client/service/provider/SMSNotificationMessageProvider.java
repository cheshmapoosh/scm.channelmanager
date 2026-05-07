package ir.daneshrefah.scm.notification.client.service.provider;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.common.model.notification.NotificationMessage;
import ir.daneshrefah.scm.common.model.notification.constants.NotificationMedia;
import ir.daneshrefah.scm.common.model.notification.constants.NotificationType;
import jakarta.jms.TextMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-18
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class SMSNotificationMessageProvider implements NotificationMessageProvider {

    @Qualifier("smsJmsTemplate")
    private final JmsTemplate smsJmsTemplate;

    @Override
    public void send(NotificationMessage notificationMessage) {
        /*
            MQ support just this 4 properties
        {
            "messageType": "authentication",
             "mobileNumber": "0912***2672",
             "message": "متن پیام ارسالی",
             "date": "2024-06-10T13:40:52.528065500"
        }

        *** >> messageId,name,time,channelCode prevent sending to mq
        */
        ObjectNode jsonMessage = JsonNodeFactory.instance.objectNode();
        jsonMessage.put("messageType", NotificationType.SMS_MQ_DEFAULT.getType());
        jsonMessage.put("mobileNumber", notificationMessage.getRequest().getRecipient().getAddress());
        jsonMessage.put("date", LocalDateTime.now().toString());
        jsonMessage.put("message", notificationMessage.getPayload());
        smsJmsTemplate.send(session -> {
            TextMessage textMessage = session.createTextMessage();
            String text = jsonMessage.toString();
            textMessage.setText(text);
            textMessage.setJMSCorrelationID(java.util.UUID.randomUUID().toString());
            log.trace("send text message: {}", text);
            return textMessage;
        });
    }

    @Override
    public boolean supports(NotificationMessage notificationMessage) {
        return Objects.nonNull(notificationMessage) && Objects.nonNull(notificationMessage.getRequest()) &&
               NotificationMedia.SMS.equals(notificationMessage.getRequest().getMedia());
    }

}

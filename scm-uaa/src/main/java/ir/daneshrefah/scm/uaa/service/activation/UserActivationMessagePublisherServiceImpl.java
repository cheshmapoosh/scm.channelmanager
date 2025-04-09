package ir.daneshrefah.scm.uaa.service.activation;

import ir.daneshrefah.scm.cache.client.connector.QueueTemplate;
import ir.daneshrefah.scm.cache.client.model.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static ir.daneshrefah.scm.uaa.utils.Constants.ACTIVATION_PUSH_SUB_QUEUE_NAME;

@Service
@RequiredArgsConstructor
public class UserActivationMessagePublisherServiceImpl implements UserActivationMessagePublisherService {

    private final QueueTemplate queueTemplate;

    @Override
    public void publish(String username, String fromTerminal) {
        Message<String> message = new Message<>();
        message.setPayload(username);
        Map<String, String> attributes = new HashMap<>();
        attributes.put("fromTerminal", fromTerminal);
        message.setAttributes(attributes);
        queueTemplate.push(message, ACTIVATION_PUSH_SUB_QUEUE_NAME);
    }
}

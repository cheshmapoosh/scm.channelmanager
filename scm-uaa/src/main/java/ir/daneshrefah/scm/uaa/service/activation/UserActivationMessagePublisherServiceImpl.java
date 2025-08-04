package ir.daneshrefah.scm.uaa.service.activation;

import ir.daneshrefah.scm.cache.client.connector.QueueTemplate;
import ir.daneshrefah.scm.cache.client.model.Message;
import ir.daneshrefah.scm.common.constant.TerminalType;
import ir.daneshrefah.scm.common.data.service.person.PersonService;
import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.uaa.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static ir.daneshrefah.scm.uaa.utils.Constants.ACTIVATION_PUSH_SUB_QUEUE_NAME;

@Service
@RequiredArgsConstructor
public class UserActivationMessagePublisherServiceImpl implements UserActivationMessagePublisherService {

    private final QueueTemplate queueTemplate;
    private final PersonService personService;
    private final UserService userService;
    private final UserChannelActivationNotifierService userChannelActivationNotifierService;

    @Override
    public void publish(String username, String fromTerminal) {
        TerminalType terminal = TerminalType.fromCode(fromTerminal).orElseThrow(() -> new InvalidInputException("Invalid terminal code " + fromTerminal));
        Message<String> message = new Message<>();
        message.setPayload(username);
        Map<String, String> attributes = new HashMap<>();
        attributes.put("fromTerminal", fromTerminal);
        message.setAttributes(attributes);
        queueTemplate.push(message, ACTIVATION_PUSH_SUB_QUEUE_NAME);
        userService
                .findByNicknameAndLegacyTerminalId(username, terminal.getLegacyTerminalId().intValue())
                .stream()
                .findFirst()
                .flatMap(user -> personService.findPersonByUsername(user.getPerson().getUsername())).ifPresent(person -> {
                    userChannelActivationNotifierService.sendRegisteredRequestNotification(person, username,terminal,TerminalType.NIB);
                });
    }
}

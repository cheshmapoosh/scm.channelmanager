package ir.daneshrefah.scm.uaa.service.logout;

import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.config.mq.LogoutJmsConfigProperties;
import jakarta.jms.Destination;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class LogoutService {
    public static final String DOUBLE_COLON = "::";
    private final JmsTemplate logoutJmsTemplate;
    private final Destination logoutTopic;
    private final LogoutJmsConfigProperties properties;

    @Async
    public void sendLogoutMessage(Authentication authentication) {
        try {
            if (authentication != null && authentication.isAuthenticated() && properties.getEnabled()) {
                UserAuthentication userAuthentication = (UserAuthentication) authentication;
                User user = userAuthentication.getPrincipal();
                String value = user.getNickname() + DOUBLE_COLON + user.getTerminalCode();
                logoutJmsTemplate.send(logoutTopic, session -> session.createTextMessage(value));
            }
        } catch (Exception e) {
            log.error("Failed to send logout message to JMS queue", e);
        }
    }
}

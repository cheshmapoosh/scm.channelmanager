package ir.daneshrefah.scm.uaa.common.service;

import ir.daneshrefah.scm.uaa.common.config.LogoutJmsConfigProperties;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalUserDetails;
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
                TerminalUserDetails terminalUserDetails = (TerminalUserDetails) authentication.getPrincipal();
                User user = terminalUserDetails.getUser();
                String value = user.getNickname() + DOUBLE_COLON + user.getTerminalCode();
                logoutJmsTemplate.send(logoutTopic, session -> session.createTextMessage(value));
            }
        } catch (Exception e) {
            log.error("Failed to send logout message to JMS queue", e);
        }
    }
}

package ir.daneshrefah.scm.uaa.security.listener;

import ir.daneshrefah.scm.common.service.TerminalService;
import ir.daneshrefah.scm.common.model.notification.DataKey;
import ir.daneshrefah.scm.common.model.notification.NotificationData;
import ir.daneshrefah.scm.common.model.notification.NotificationMedia;
import ir.daneshrefah.scm.common.model.notification.NotificationRequest;
import ir.daneshrefah.scm.common.model.notification.constants.NotificationConstants;
import ir.daneshrefah.scm.notification.client.spec.NotificationService;
import ir.daneshrefah.scm.uaa.common.model.user.User;

import ir.daneshrefah.scm.uaa.security.token.PostAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-27
 */
@RequiredArgsConstructor
@Component
public class AuthenticationNotificationEventListener extends BaseAuthenticationListener {

    private final TerminalService terminalService;
    private final NotificationService notificationService;

    @Override
    protected void onSuccessAuthenticationEvent(PostAuthenticationToken authentication) {
        if (!authentication.isNotificationRequired()) {
            return;
        }
        User user = authentication.getPrincipal().getUser();
//        BeanUtils.describe(authentication);
        terminalService
                .findTerminalByCode(user.getTerminalCode())
                .ifPresent(terminal -> {
                    NotificationData data = new NotificationData()
                            .put(DataKey.TITLE, "")
                            .put(DataKey.LOGIN_TIME, authentication.getIssuedAt())
                            .put(DataKey.TERMINAL_CODE, terminal.getCode())
                            .put(DataKey.TERMINAL_TITLE, terminal.getTitle());
                    NotificationRequest request = NotificationRequest.builder()
                            .media(NotificationMedia.SMS)
                            .username(Objects.nonNull(user.getPerson()) ? user.getPerson().getUsername() : null)
                            .recipient(user.getPerson().getMobile1())
                            .data(data)
                            .messageTemplateCode(NotificationConstants.MESSAGE_TEMPLATE_CODE_AUTHENTICATION)
                            .expiration(LocalDateTime.now().plusHours(1))
                            .build();
//                    notificationService.sendNotification(request);
                });
    }


}

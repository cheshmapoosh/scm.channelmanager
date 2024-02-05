package ir.daneshrefah.scm.uaa.security.listener;

import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.common.service.TerminalService;
import ir.daneshrefah.scm.uaa.domain.notification.NotificationData;
import ir.daneshrefah.scm.uaa.domain.notification.NotificationMedia;
import ir.daneshrefah.scm.uaa.domain.notification.NotificationRequest;
import ir.daneshrefah.scm.uaa.security.token.PostAuthenticationToken;
import ir.daneshrefah.scm.uaa.service.notification.NotificationConstants;
import ir.daneshrefah.scm.uaa.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

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
        Optional<Terminal> terminal = terminalService.findTerminalByCode(authentication.getDetails().getUser().getTerminalCode());
//        BeanUtils.describe(authentication);
        NotificationData data = new NotificationData()
                .put("title", "")
                .put("loginTime", authentication.getIssuedAt())
                .put("terminalCode", terminal.get().getCode())
                .put("terminalTitle", terminal.get().getTitle());
        NotificationRequest request = NotificationRequest.builder()
                .media(NotificationMedia.SMS)
                .recipient(authentication.getDetails().getUser().getPerson().getMobile1())
                .data(data)
                .messageTemplateCode(NotificationConstants.MESSAGE_TEMPLATE_CODE_AUTHENTICATION)
                .build();
        notificationService.sendNotification(request);
    }

}

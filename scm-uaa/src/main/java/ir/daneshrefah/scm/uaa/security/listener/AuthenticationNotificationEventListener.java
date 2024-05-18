package ir.daneshrefah.scm.uaa.security.listener;

import ir.daneshrefah.scm.common.model.message.IssuerInfo;
import ir.daneshrefah.scm.common.model.notification.constants.DataKey;
import ir.daneshrefah.scm.common.model.notification.NotificationData;
import ir.daneshrefah.scm.common.model.notification.constants.NotificationMedia;
import ir.daneshrefah.scm.common.model.notification.NotificationRequest;
import ir.daneshrefah.scm.common.model.notification.constants.NotificationTemplate;
import ir.daneshrefah.scm.common.service.terminal.TerminalService;
import ir.daneshrefah.scm.notification.client.service.spec.NotificationService;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.security.token.PostAuthenticationToken;
import ir.daneshrefah.scm.utils.date.DateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
                            .put(DataKey.LOGIN_TIME, getShamsiLoginTime(authentication))
                            .put(DataKey.TERMINAL_TITLE, terminal.getTitle())
                            .put(DataKey.OTP_CODE, "123");
                    IssuerInfo issuerInfo = IssuerInfo.builder()
                            .parentCorrelationId(authentication.getSessionId())
                            .nickname(user.getNickname())
                            .username(Objects.nonNull(user.getPerson()) ? user.getPerson().getUsername() : null)
                            .personType(user.getPerson().getPersonType())
                            .terminalCode(user.getTerminalCode())
//                            .hostAddress()
//                            .instanceName()
                            .build();
                    NotificationRequest request = NotificationRequest.builder()
                            .template(NotificationTemplate.AUTHENTICATION)
                            .media(NotificationMedia.SMS)
                            .recipient(user.getPerson().getMobile1())
                            .recipientType(user.getPerson().getPersonType())
                            .recipientUsername(Objects.nonNull(user.getPerson()) ? user.getPerson().getUsername() : null)
                            .data(data)
                            .terminalCode(user.getTerminalCode())
                            .issuerInfo(issuerInfo)
                            .build();
                    notificationService.sendNotification(request);
                });
    }

    private String getShamsiLoginTime(PostAuthenticationToken authentication) {
        return DateUtils
                .ShamsiCalendarConvertor
                .convertToShamsiDateString(DateUtils
                                .DateConverter
                                .convertToLocalDateTime(DateUtils.DateConverter
                                                .convertToTimestamp(authentication.getIssuedAt())), "yyyy/MM/dd HH:mm:ss");
    }


}

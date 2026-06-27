package ir.daneshrefah.scm.uaa.service.activation.nib;

import ir.daneshrefah.scm.common.constant.TerminalType;
import ir.daneshrefah.scm.common.dto.terminal.TerminalService;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.model.message.IssuerInfo;
import ir.daneshrefah.scm.common.model.notification.NotificationData;
import ir.daneshrefah.scm.common.model.notification.NotificationRequest;
import ir.daneshrefah.scm.common.model.notification.constants.NotificationDataKey;
import ir.daneshrefah.scm.common.model.notification.constants.NotificationMedia;
import ir.daneshrefah.scm.common.model.notification.constants.NotificationTemplate;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.common.model.recipient.Recipient;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.common.model.user.UserIdentifierType;
import ir.daneshrefah.scm.notification.client.service.spec.NotificationService;
import ir.daneshrefah.scm.utils.date.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserChannelActivationNotifierServiceImpl implements UserChannelActivationNotifierService {

    private final NotificationService notificationService;
    private final TerminalService terminalService;

    @Override
    public void sendSuccessNotification(GeneralPerson person, String sourceNickname, TerminalType sourceChannel, TerminalType targetChannel) {
        sendNotification(person, sourceChannel, targetChannel, sourceNickname, NotificationTemplate.CHANNEL_ACTIVATION_SUCCESS);
    }

    @Override
    public void sendFailedNotification(GeneralPerson person, String sourceNickname, TerminalType sourceChannel, TerminalType targetChannel) {
        sendNotification(person, sourceChannel, targetChannel, sourceNickname, NotificationTemplate.CHANNEL_ACTIVATION_FAILED);
    }

    @Override
    public void sendRegisteredRequestNotification(GeneralPerson person, String sourceNickname, TerminalType sourceChannel, TerminalType targetChannel) {
        sendNotification(person, sourceChannel, targetChannel, sourceNickname, NotificationTemplate.CHANNEL_ACTIVATION_REQUEST);
    }

    private void sendNotification(GeneralPerson person, TerminalType sourceChannel, TerminalType targetChannel, String nickname, NotificationTemplate notificationTemplate) {
        try {
            Terminal sourceTerminal = terminalService.findTerminalByLegacyId(sourceChannel.getLegacyTerminalId().intValue()).orElseThrow(() -> new NoMatchRecordFoundException("terminal"));
            Terminal targetTerminal = terminalService.findTerminalByLegacyId(targetChannel.getLegacyTerminalId().intValue()).orElseThrow(() -> new NoMatchRecordFoundException("terminal"));
            NotificationData notificationData = createNotificationData(nickname, targetTerminal);
            //CREATE ISSUER INFO
            IssuerInfo issuerInfo = IssuerInfo.builder()
                    .personType(person.getPersonType())
                    .personUsername(person.getUsername())
                    .terminalCode(sourceTerminal.getCode())
                    .build();
            //CREATE RECIPIENT
            Recipient recipient = Recipient.builder()
                    .address(person.getMobile1())
                    .identifier(nickname)
                    .identifierType(UserIdentifierType.USER_NICKNAME)
                    .terminalCode(sourceTerminal.getCode())
                    .build();
            //CREATE NOTIFICATION REQUEST
            NotificationRequest request = NotificationRequest.builder()
                    .template(notificationTemplate)
                    .media(NotificationMedia.SMS)
                    .recipient(recipient)
                    .userLocale(new Locale("fa", "IR")) //TODO GET FROM HEADER
                    .data(notificationData)
                    .terminalCode(sourceTerminal.getCode())
                    .issuerInfo(issuerInfo)
                    .build();
            notificationService.sendNotification(request);
        } catch (Exception e) {
            log.error("Exception occurred while sending notification: {}", safeMessage(e));
        }
    }

    private NotificationData createNotificationData(String nickName, Terminal terminal) {
        return new NotificationData()
                .put(NotificationDataKey.USER_NICKNAME, nickName)
                .put(NotificationDataKey.CHANNEL_TITLE, terminal.getTitle())
                .put(NotificationDataKey.TIME, getShamsiCurrentTime());
    }

    private String getShamsiCurrentTime() {
        return DateUtils
                .ShamsiCalendarConvertor
                .convertToShamsiDateString(DateUtils
                        .DateConverter
                        .convertToLocalDateTime(DateUtils.DateConverter
                .convertToTimestamp(Instant.now())), "yyyy/MM/dd HH:mm:ss");
    }

    private String safeMessage(Exception exception) {
        if (exception == null || exception.getMessage() == null) {
            return exception == null ? null : exception.getClass().getSimpleName();
        }
        String message = exception.getMessage()
                .replace('\r', ' ')
                .replace('\n', ' ')
                .replaceAll("(?i)(password|token|authorization|client_secret|authorization_code|pin|otp|session[_-]?id|card[_-]?number)\\s*[:=]\\s*\\S+", "$1=***")
                .trim();
        return message.length() > 300 ? message.substring(0, 300) : message;
    }
}

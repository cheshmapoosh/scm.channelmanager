package ir.daneshrefah.scm.uaa.service.activation.pwa.services.activation;

import ir.daneshrefah.scm.common.constant.AccessibleLocale;
import ir.daneshrefah.scm.common.model.message.IssuerInfo;
import ir.daneshrefah.scm.common.model.notification.NotificationData;
import ir.daneshrefah.scm.common.model.notification.NotificationRequest;
import ir.daneshrefah.scm.common.model.notification.constants.NotificationDataKey;
import ir.daneshrefah.scm.common.model.notification.constants.NotificationMedia;
import ir.daneshrefah.scm.common.model.notification.constants.NotificationTemplate;
import ir.daneshrefah.scm.common.model.person.PersonType;
import ir.daneshrefah.scm.common.model.recipient.Recipient;
import ir.daneshrefah.scm.common.model.user.UserIdentifierType;
import ir.daneshrefah.scm.notification.client.service.spec.NotificationService;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.domain.pwa.PwaLogin;
import ir.daneshrefah.scm.uaa.domain.pwa.UserActivation;
import ir.daneshrefah.scm.uaa.service.activation.pwa.model.ActivationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Component
@RequiredArgsConstructor
@Slf4j
public class PwaNotificationCenter {

    private final NotificationService notificationService;

    public void sendActivationOtp(User user, ActivationRequest request, UserActivation userActivation) {
        try {
            //CREATE ISSUER INFO
            IssuerInfo issuerInfo = IssuerInfo.builder()
                    .personType(user.getPerson().getPersonType())
                    .personUsername(user.getPerson().getUsername())
                    .terminalCode(request.getRequestHeaders().getChannel())
                    .build();
            //CREATE RECIPIENT
            Recipient recipient = Recipient.builder()
                    .address(request.getPhoneNumber())
                    .identifier(request.getPhoneNumber())
                    .identifierType(UserIdentifierType.MOBILE_NUMBER)
                    .terminalCode(request.getRequestHeaders().getChannel())
                    .build();
            //CREATE NOTIFICATION REQUEST
            NotificationRequest notificationRequest = NotificationRequest.builder()
                    .template(NotificationTemplate.MB_ACTIVATION_CODE)
                    .media(NotificationMedia.SMS)
                    .recipient(recipient)
                    .userLocale(new Locale("fa", "IR")) //TODO GET FROM HEADER
                    .data(createActivationCodeNotificationData(user,request,userActivation))
                    .terminalCode(request.getRequestHeaders().getChannel())
                    .issuerInfo(issuerInfo)
                    .build();
            notificationService.sendNotification(notificationRequest);
        } catch (Exception e) {
            log.error("Exception occurred while sending notification: {}", safeMessage(e));
        }

    }

    private NotificationData createActivationCodeNotificationData(User user, ActivationRequest request, UserActivation userActivation) {
        return new NotificationData()
                .put(NotificationDataKey.HASH_CODE, request.getRequestHeaders().getHashCode())
                .put(NotificationDataKey.CODE, userActivation.getActivationCode());
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendActivationBlockedNotification(ActivationRequest request, String failedTrials, String blockedTime) {
        try {
            NotificationData  notificationData = createRegisterBlockNotificationData(request.getUsername(), blockedTime, failedTrials, request.getPhoneNumber());
            NotificationTemplate template = NotificationTemplate.REGISTER_BLOCKED_MESSAGE;
            //CREATE ISSUER INFO
            IssuerInfo issuerInfo = IssuerInfo.builder()
                    .personType(PersonType.UNKNOWN)
                    .personUsername(request.getUsername())
                    .terminalCode(request.getRequestHeaders().getChannel())
                    .build();
            //CREATE RECIPIENT
            Recipient recipient = Recipient.builder()
                    .address(request.getPhoneNumber())
                    .identifier(request.getPhoneNumber())
                    .identifierType(UserIdentifierType.MOBILE_NUMBER)
                    .terminalCode(request.getRequestHeaders().getChannel())
                    .build();
            //CREATE NOTIFICATION REQUEST
            NotificationRequest notificationRequest = NotificationRequest.builder()
                    .template(template)
                    .media(NotificationMedia.SMS)
                    .recipient(recipient)
                    .userLocale(new Locale("fa", "IR")) //TODO GET FROM HEADER
                    .data(notificationData)
                    .terminalCode(request.getRequestHeaders().getChannel())
                    .issuerInfo(issuerInfo)
                    .build();
            notificationService.sendNotification(notificationRequest);
        } catch (Exception e) {
            log.error("Exception occurred while sending notification: {}", safeMessage(e));
        }
    }


    private NotificationData createLoginBlockNotificationData(String nickname, String blockedTime, String trails) {
        return createBaseNotificationData(nickname, blockedTime, trails);
    }

    private NotificationData createRegisterBlockNotificationData(String nickname, String blockedTime, String trails, String mobile) {
        NotificationData baseNotificationData = createBaseNotificationData(nickname, blockedTime, trails);
        baseNotificationData.put(NotificationDataKey.MOBILE, mobile);
        return baseNotificationData;
    }

    private NotificationData createBaseNotificationData(String nickname, String blockedTime, String trails) {
        return new NotificationData()
                .put(NotificationDataKey.USER_NICKNAME, nickname)
                .put(NotificationDataKey.BLOCKED_TIME, blockedTime)
                .put(NotificationDataKey.TRAILS, trails);
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendLoginBlockedNotification(PwaLogin login, String failedTrials, String blockedTime) {
        try {
            NotificationData notificationData = createLoginBlockNotificationData(login.getUsername(), failedTrials, blockedTime);
            NotificationTemplate  template = NotificationTemplate.LOGIN_BLOCKED_MESSAGE;
            //CREATE ISSUER INFO
            IssuerInfo issuerInfo = IssuerInfo.builder()
                    .personType(PersonType.UNKNOWN)
                    .personUsername(login.getUsername())
                    .terminalCode(login.getDeviceModel())
                    .build();
            //CREATE RECIPIENT
            Recipient recipient = Recipient.builder()
                    .address(login.getPhoneNumber())
                    .identifier(login.getPhoneNumber())
                    .identifierType(UserIdentifierType.MOBILE_NUMBER)
                    .terminalCode(login.getDeviceModel())
                    .build();
            //CREATE NOTIFICATION REQUEST
            NotificationRequest notificationRequest = NotificationRequest.builder()
                    .template(template)
                    .media(NotificationMedia.SMS)
                    .recipient(recipient)
                    .userLocale(AccessibleLocale.FA_IR.getLocale()) //TODO GET FROM HEADER
                    .data(notificationData)
                    .terminalCode(login.getDeviceModel())
                    .issuerInfo(issuerInfo)
                    .build();
            notificationService.sendNotification(notificationRequest);
        } catch (Exception e) {
            log.error("Exception occurred while sending notification: {}", safeMessage(e));
        }
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

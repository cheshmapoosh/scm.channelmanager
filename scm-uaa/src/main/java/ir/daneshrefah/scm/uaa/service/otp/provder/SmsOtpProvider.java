package ir.daneshrefah.scm.uaa.service.otp.provder;

import ir.daneshrefah.scm.cache.client.connector.CacheTemplate;
import ir.daneshrefah.scm.common.constant.otp.OtpReasonDictionary;
import ir.daneshrefah.scm.common.constant.otp.OtpType;
import ir.daneshrefah.scm.common.dto.terminal.TerminalService;
import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.model.notification.NotificationData;
import ir.daneshrefah.scm.common.model.notification.NotificationRequest;
import ir.daneshrefah.scm.common.model.notification.constants.NotificationDataKey;
import ir.daneshrefah.scm.common.model.notification.constants.NotificationMedia;
import ir.daneshrefah.scm.common.model.recipient.Recipient;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.common.model.user.UserIdentifierType;
import ir.daneshrefah.scm.notification.client.service.spec.NotificationService;
import ir.daneshrefah.scm.uaa.config.OtpProperties;
import ir.daneshrefah.scm.uaa.exception.InvalidOtpCodeException;
import ir.daneshrefah.scm.uaa.exception.OtpNotFoundException;
import ir.daneshrefah.scm.uaa.service.otp.dto.*;
import ir.daneshrefah.scm.uaa.utils.ProfileInfo;
import ir.daneshrefah.scm.utils.date.DateUtils;
import ir.daneshrefah.scm.utils.string.StringUtils;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

import static ir.daneshrefah.scm.common.constant.CacheConstants.CACHE_NAME_OTP;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-30
 */
@Component
@Slf4j
public class SmsOtpProvider extends AbstractOtpProvider {

    private final NotificationService notificationService;
    private final TerminalService terminalService;

    public SmsOtpProvider(
            CacheTemplate cacheTemplate,
            OtpProperties otpProperties,
            NotificationService notificationService,
            ProfileInfo profileInfo,
            TerminalService terminalService) {
        super(cacheTemplate, otpProperties, profileInfo);
        this.notificationService = notificationService;
        this.terminalService = terminalService;
    }

    @Override
    public OtpSendResponse sendOtpInternal(OtpSendRequest request) {
        validateRequest(request);
        Otp otp = buildOtpInstance(request, true);
        if (!otp.isDelivered()) {
            sendNotification(otp);
            otp = deliverOtp(otp);
            log.trace("sent otp {}", otp.toString());
        }
        return OtpSendResponse.builder()
                .otp(otp)
                .isSuccessful(true)
                .errorMessage(null)
                .build();
    }

    @Override
    protected String buildRecipientAddress(Recipient recipient) {
        return StringUtils.normalizePhoneNumber(recipient.getAddress());
    }

    @Override
    protected Object buildRecipientIdentifier(Recipient recipient) {
        if (UserIdentifierType.MOBILE_NUMBER.equals(recipient.getIdentifierType())) {
            return StringUtils.normalizePhoneNumber(recipient.getIdentifier());
        }
        return recipient.getIdentifier();
    }

    private void validateRequest(OtpSendRequest request) {
//        TODO check cell phone number pattern
        String mobileNumber = request.getRecipient().getAddress();
        ValidationUtils.checkInvalidMobileNumber(mobileNumber, () -> new InvalidInputException("mobile"));
    }

    private void sendNotification(Otp otp) {
        Terminal terminal = terminalService.findTerminalByCode(otp.getRecipient().getTerminalCode()).orElseThrow(InvalidOtpCodeException::new);
        NotificationData data = new NotificationData();
        if (otp.getMetadata() != null) {
            otp.getMetadata().keySet().forEach(key -> {
                NotificationDataKey dataKey = NotificationDataKey.findByCode(key);
                if (dataKey == null) {
                    throw new InvalidInputException("Invalid data key");
                }
                data.put(dataKey, otp.getMetadata().get(key));
            });
        }
        data.put(NotificationDataKey.OTP_CODE, otp.getOtpCode());
        data.put(NotificationDataKey.TERMINAL_TITLE, terminal.getTitle());
        data.put(NotificationDataKey.LOGIN_TIME, nowShamsiLoginTime());
        data.put(NotificationDataKey.REASON, OtpReasonDictionary.getOtpReasonDictionary(otp.getReason()).getPersian()); //TODO GET FROM LOCALE
        NotificationRequest request = NotificationRequest.builder()
                .template(otp.getReason().getNotificationTemplate())
                .media(NotificationMedia.SMS)
                .userLocale(new Locale("fa", "IR")) //TODO GET FROM HEADER
                .recipient(otp.getRecipient())
                .data(data)
                .terminalCode(otp.getRecipient().getTerminalCode())
                .issuerInfo(otp.getIssuer())
                .build();
        notificationService.sendNotification(request);
    }

    @Override
    public OtpVerifyResponse verifyOtp(OtpVerifyRequest request) {
        ValidationUtils.checkNull(request, () -> new MissingRequiredInputException("request"));
        String otpKey = extractOtpKey(request);
        Otp otp = (Otp) cacheTemplate.getFromCache(CACHE_NAME_OTP, otpKey);
        ValidationUtils.checkNull(otp, OtpNotFoundException::new);

        if (Objects.equals(Otp.OtpStatus.PENDING, otp.status())) {
            throw new InvalidOtpCodeException("Not send otp");
        }

        if (Objects.equals(Otp.OtpStatus.EXPIRED, otp.status())) {
            cacheTemplate.removeFromCache(CACHE_NAME_OTP, otpKey);
            throw new InvalidOtpCodeException("Expired otp");
        }

        ValidationUtils.checkNotEqualsString(otp.getOtpCode(), request.getClaimCode(), () -> {
            otp.plusFailedCount();
            if (Objects.equals(Otp.OtpStatus.MAX_ATTEMPTS_FAILED, otp.status())) {
                cacheTemplate.removeFromCache(CACHE_NAME_OTP, otpKey);
                return new InvalidOtpCodeException("max attempts failed");
            } else {
                cacheTemplate.putInCache(CACHE_NAME_OTP, otpKey, otp);
                return new InvalidOtpCodeException("Invalid otp code");
            }
        });

        otp.plusReusedCount();
        if (Objects.equals(Otp.OtpStatus.MAX_ATTEMPTS_REUSED, otp.status())) {
            cacheTemplate.removeFromCache(CACHE_NAME_OTP, otpKey);
            throw new InvalidOtpCodeException("max attempts reused");
        } else {
            cacheTemplate.putInCache(CACHE_NAME_OTP, otpKey, otp);
        }
        log.trace("verify otp is successful for key {}", otp.getKey());
        return OtpVerifyResponse.builder()
                .isSuccessful(true)
                .build();
    }

    @Override
    public OtpType getType() {
        return OtpType.SMS;
    }

    private String nowShamsiLoginTime() {
        return DateUtils
                .ShamsiCalendarConvertor
                .convertToShamsiDateString(DateUtils
                        .DateConverter
                        .convertToLocalDateTime(DateUtils.DateConverter
                                .convertToTimestamp(Instant.now())), "yyyy/MM/dd HH:mm:ss");
    }

}

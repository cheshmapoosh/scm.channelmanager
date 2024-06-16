package ir.daneshrefah.scm.uaa.service.otp.provder;

import ir.daneshrefah.scm.cache.client.connector.CacheTemplate;
import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.model.notification.NotificationData;
import ir.daneshrefah.scm.common.model.notification.NotificationRequest;
import ir.daneshrefah.scm.common.model.notification.constants.NotificationDataKey;
import ir.daneshrefah.scm.common.model.notification.constants.NotificationMedia;
import ir.daneshrefah.scm.common.model.recipient.Recipient;
import ir.daneshrefah.scm.common.model.user.UserIdentifierType;
import ir.daneshrefah.scm.notification.client.service.spec.NotificationService;
import ir.daneshrefah.scm.uaa.config.OtpProperties;
import ir.daneshrefah.scm.uaa.domain.otp.OtpType;
import ir.daneshrefah.scm.uaa.exception.InvalidOtpCodeException;
import ir.daneshrefah.scm.uaa.exception.OtpNotFoundException;
import ir.daneshrefah.scm.uaa.service.otp.dto.*;
import ir.daneshrefah.scm.utils.string.StringUtils;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import ir.daneshrefah.scm.utils.validation.regex.CommonRegex;
import org.springframework.stereotype.Component;

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
public class SmsOtpProvider extends AbstractOtpProvider {

    private final NotificationService notificationService;

    public SmsOtpProvider(CacheTemplate cacheTemplate, OtpProperties otpProperties, NotificationService notificationService) {
        super(cacheTemplate, otpProperties);
        this.notificationService = notificationService;
    }

    @Override
    public OtpSendResponse sendOtp(OtpSendRequest request) {
        validateRequest(request);
        Otp otp = buildOtpInstance(request, true);
        if (!otp.isDelivered()) {
            sendNotification(otp);
            otp = deliverOtp(otp);
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
        ValidationUtils.checkRegex(CommonRegex.MOBILE_NUMBER_REGEX,mobileNumber,()-> new InvalidInputException("mobile"));
    }

    private void sendNotification(Otp otp) {
        NotificationData data = new NotificationData();
        data.put(NotificationDataKey.OTP_CODE, otp.getOtpCode());
        NotificationRequest request = NotificationRequest.builder()
                .template(otp.getReason().getNotificationTemplate())
                .userLocale(new Locale("fa","IR")) //TODO GET FROM REQUEST HEADER
                .media(NotificationMedia.SMS)
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
        return OtpVerifyResponse.builder()
                .isSuccessful(true)
                .build();
    }

    @Override
    public OtpType getType() {
        return OtpType.SMS;
    }

}

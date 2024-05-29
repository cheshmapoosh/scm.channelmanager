package ir.daneshrefah.scm.uaa.service.otp.provder;

import ir.daneshrefah.scm.cache.client.connector.CacheTemplate;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.model.notification.NotificationData;
import ir.daneshrefah.scm.common.model.notification.NotificationRequest;
import ir.daneshrefah.scm.common.model.notification.constants.NotificationMedia;
import ir.daneshrefah.scm.common.model.recipient.Recipient;
import ir.daneshrefah.scm.notification.client.service.spec.NotificationService;
import ir.daneshrefah.scm.uaa.config.OtpProperties;
import ir.daneshrefah.scm.uaa.domain.otp.OtpType;
import ir.daneshrefah.scm.uaa.exception.InvalidOtpCodeException;
import ir.daneshrefah.scm.uaa.exception.OtpNotFoundException;
import ir.daneshrefah.scm.uaa.service.otp.dto.*;
import ir.daneshrefah.scm.utils.string.StringUtils;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import org.springframework.stereotype.Component;

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
        return StringUtils.normalizePhoneNumber(recipient.getIdentifier());
    }

    private void validateRequest(OtpSendRequest request) {
//        TODO check cell phone number pattern
    }

    private void sendNotification(Otp otp) {
        NotificationData data = new NotificationData();

        NotificationRequest request = NotificationRequest.builder()
                .template(otp.getReason().getNotificationTemplate())
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
        ValidationUtils.checkNull(otp, () -> new OtpNotFoundException());
        ValidationUtils.checkNotEqualsString(otp.getOtpCode(), request.getClaimCode(), () -> new InvalidOtpCodeException());
        //TODO increase tryCount
        return OtpVerifyResponse.builder()
                .isSuccessful(true)
                .build();
    }

    @Override
    public OtpType getType() {
        return OtpType.SMS;
    }

}

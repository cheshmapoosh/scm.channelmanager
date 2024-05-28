package ir.daneshrefah.scm.uaa.service.otp.provder;

import ir.daneshrefah.scm.cache.client.connector.CacheTemplate;
import ir.daneshrefah.scm.common.exception.MethodNotSupportedException;
import ir.daneshrefah.scm.common.model.recipient.Recipient;
import ir.daneshrefah.scm.uaa.config.OtpProperties;
import ir.daneshrefah.scm.uaa.domain.otp.OtpPattern;
import ir.daneshrefah.scm.uaa.domain.otp.OtpType;
import ir.daneshrefah.scm.uaa.exception.OtpAlreadyExistException;
import ir.daneshrefah.scm.uaa.exception.OtpCodeGenerationException;
import ir.daneshrefah.scm.uaa.service.otp.dto.*;
import ir.daneshrefah.scm.utils.date.DateUtils;
import ir.daneshrefah.scm.utils.string.StringUtils;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

import static ir.daneshrefah.scm.common.constant.CacheConstants.CACHE_NAME_OTP;
import static ir.daneshrefah.scm.utils.string.StringUtils.upperCase;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-30
 */
@RequiredArgsConstructor
public abstract class AbstractOtpProvider {

    protected final CacheTemplate cacheTemplate;
    private final OtpProperties otpProperties;

    public void activate() {
        throw new MethodNotSupportedException("otp");
    }

    public OtpSendResponse sendOtp(OtpSendRequest request) {
        throw new MethodNotSupportedException("otp");
    }

    protected final String extractOtpKey(OtpBaseRequest request) {
        return StringUtils.joinWith(StringUtils.DASH, upperCase(request.getRecipient().getTerminalCode()), request.getOtpType().name(),
                request.getRecipient().getIdentifierType().name(), buildRecipientIdentifier(request.getRecipient()),
                buildRecipientAddress(request.getRecipient()), request.getReason().name());
    }

    protected Object buildRecipientIdentifier(Recipient recipient) {
        return recipient.getIdentifier();
    }

    protected String buildRecipientAddress(Recipient recipient) {
        return recipient.getAddress();
    }

    protected final Otp buildOtpInstance(OtpSendRequest request, boolean requireDeliver) {
        String otpKey = extractOtpKey(request);
        Otp otp = (Otp) cacheTemplate.getFromCache(CACHE_NAME_OTP, otpKey);
        ValidationUtils.checkNonNull(otp, () -> new OtpAlreadyExistException());
        String otpCode = generateOtpCode(request.getReason().getPattern(), request.getReason().getCount());
        ValidationUtils.checkBlankString(otpCode, () -> new OtpCodeGenerationException());
        otp = Otp.builder()
                .key(otpKey)
                .otpType(request.getOtpType())
                .reason(request.getReason())
                .recipient(request.getRecipient())
                .issuer(request.getIssuer())
                .otpCode(otpCode)
                .expireTime(DateUtils.InstantTools.plusMinutesToCurrent(request.getReason().getTimeToLiveMinutes()))
                .isDelivered(!requireDeliver)
                .build();
        cacheTemplate.putInCacheIfAbsent(CACHE_NAME_OTP, otpKey, otp, otp.getReason().getTimeToLiveMinutes());
        return otp;
    }

    protected Otp deliverOtp(Otp otp) {
        if (Objects.isNull(otp) || otp.isDelivered()) {
            return otp;
        }
        otp.setDelivered(true);
        cacheTemplate.putInCacheIfAbsent(CACHE_NAME_OTP, otp.getKey(), otp, otp.getReason().getTimeToLiveMinutes());
        return otp;
    }

    private String generateOtpCode(OtpPattern pattern, int count) {
        if (Objects.isNull(pattern) || count < 1) {
            return null;
        }
        switch (pattern) {
            case NUMERIC:
                return StringUtils.randomNumeric(count);
            case ALPHABETIC:
                return StringUtils.randomAlphabetic(count);
            case ALPHA_NUMERIC:
                return StringUtils.randomAlphanumeric(count);
        }
        return null;
    }

    public abstract OtpVerifyResponse verifyOtp(OtpVerifyRequest request);

    public abstract OtpType getType();

}

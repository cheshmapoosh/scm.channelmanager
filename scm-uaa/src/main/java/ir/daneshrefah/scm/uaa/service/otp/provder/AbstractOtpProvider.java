package ir.daneshrefah.scm.uaa.service.otp.provder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ir.daneshrefah.scm.cache.client.connector.CacheTemplate;
import ir.daneshrefah.scm.common.constant.otp.OtpPattern;
import ir.daneshrefah.scm.common.constant.otp.OtpType;
import ir.daneshrefah.scm.common.exception.MethodNotSupportedException;
import ir.daneshrefah.scm.common.model.recipient.Recipient;
import ir.daneshrefah.scm.uaa.config.OtpProperties;
import ir.daneshrefah.scm.uaa.exception.OtpCodeGenerationException;
import ir.daneshrefah.scm.uaa.service.otp.dto.*;
import ir.daneshrefah.scm.uaa.service.user.UserService;
import ir.daneshrefah.scm.uaa.utils.ProfileInfo;
import ir.daneshrefah.scm.utils.date.DateUtils;
import ir.daneshrefah.scm.utils.string.StringUtils;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public abstract class AbstractOtpProvider {

    protected final CacheTemplate cacheTemplate;
    private final OtpProperties otpProperties;
    private final ProfileInfo profileInfo;
    protected final UserService userService;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @PostConstruct
    public void configure(){
        OBJECT_MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
    }

    public void activate() {
        throw new MethodNotSupportedException("otp");
    }

    /**
     *Provider Implementer class should override this method.
     */
    protected  OtpSendResponse sendOtpInternal(OtpSendRequest request){
        throw new MethodNotSupportedException("otp");
    }

    public OtpSendResponse sendOtp(OtpSendRequest request) {
        OtpSendResponse otpSendResponse = sendOtpInternal(request);
        return adviseOtpResponse(otpSendResponse);
    }

    @SneakyThrows
    private OtpSendResponse adviseOtpResponse(OtpSendResponse otpSendResponse) {
        if (profileInfo.isTraceMode()){
            log.info(OBJECT_MAPPER.writeValueAsString(otpSendResponse.getOtp()));
           return otpSendResponse;
        }
        return cleanResponseSecureData(otpSendResponse);
    }

    private OtpSendResponse cleanResponseSecureData(OtpSendResponse otpSendResponse) {
        Otp otp = otpSendResponse.getOtp();
        Recipient recipient = otpSendResponse.getOtp().getRecipient();
        Recipient cleanRecipient = Recipient.builder().address(StringUtils.maskPhoneNumber(recipient.getAddress())).build();
        Otp cleanOtp = Otp
                .builder()
                .recipient(cleanRecipient)
                .expireTime(otp.getExpireTime()).build();
        return OtpSendResponse
                .builder()
                .otp(cleanOtp)
                .isSuccessful(otpSendResponse.isSuccessful())
                .errorMessage(otpSendResponse.getErrorMessage())
                .build();
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
        if (Objects.nonNull(otp) && DateUtils.InstantTools.currentDate().isBefore(otp.getExpireTime())) {
            return otp;
        }
//        ValidationUtils.checkNonNull(otp, () -> new OtpAlreadyExistException());
        String otpCode = generateOtpCode(request.getReason().getPattern(), request.getReason().getLength());
        ValidationUtils.checkBlankString(otpCode, OtpCodeGenerationException::new);
        otp = createOtp(otpKey,request,otpCode,requireDeliver) ;
        cacheTemplate.putInCache(CACHE_NAME_OTP, otpKey, otp, otp.getReason().getTimeToLiveMinutes());
        return otp;
    }

    private Otp createOtp(String otpKey, OtpSendRequest request, String otpCode, boolean requireDeliver) {
            return Otp.builder()
                    .key(otpKey)
                    .otpType(request.getOtpType())
                    .reason(request.getReason())
                    .recipient(request.getRecipient())
                    .otpCode(otpCode)
                    .expireTime(DateUtils.InstantTools.plusMinutesToCurrent(request.getReason().getTimeToLiveMinutes()))
                    .isDelivered(!requireDeliver)
                    .build();
    }

    protected Otp deliverOtp(Otp otp) {
        if (Objects.isNull(otp) || otp.isDelivered()) {
            return otp;
        }
        otp.setDelivered(true);
        cacheTemplate.putInCache(CACHE_NAME_OTP, otp.getKey(), otp, otp.getReason().getTimeToLiveMinutes());
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
    public abstract OtpVerifyResponse verifyOtpByDelegatedUser(VerifyOtpByDelegatedUserRequest request);
    public abstract OtpVerifyResponse verifyOtpByLoggedInUser(VerifyOtpByLoggedInUserRequest request);
    public abstract OtpVerifyResponse verifyOtpByUsername(VerifyOtpByUsernameRequest request);
    public abstract OtpVerifyResponse verifyOtpByNickname(VerifyOtpByNicknameRequest request);
    public abstract OtpType getType();

}

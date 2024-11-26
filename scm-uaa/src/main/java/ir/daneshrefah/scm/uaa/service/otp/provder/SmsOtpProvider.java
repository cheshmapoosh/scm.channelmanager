package ir.daneshrefah.scm.uaa.service.otp.provder;

import ir.daneshrefah.scm.cache.client.connector.CacheTemplate;
import ir.daneshrefah.scm.common.constant.otp.OtpReason;
import ir.daneshrefah.scm.common.constant.otp.OtpType;
import ir.daneshrefah.scm.common.data.entity.person.GeneralPersonEntity;
import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.model.notification.NotificationData;
import ir.daneshrefah.scm.common.model.notification.NotificationRequest;
import ir.daneshrefah.scm.common.model.notification.constants.NotificationDataKey;
import ir.daneshrefah.scm.common.model.notification.constants.NotificationMedia;
import ir.daneshrefah.scm.common.model.recipient.Recipient;
import ir.daneshrefah.scm.common.model.user.AuthenticationMethod;
import ir.daneshrefah.scm.common.model.user.UserIdentifierType;
import ir.daneshrefah.scm.notification.client.service.spec.NotificationService;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.uaa.config.OtpProperties;
import ir.daneshrefah.scm.uaa.domain.otp.OtpAuthenticationType;
import ir.daneshrefah.scm.uaa.exception.InvalidOtpCodeException;
import ir.daneshrefah.scm.uaa.exception.OtpNotFoundException;
import ir.daneshrefah.scm.uaa.repository.authentication.UserEntity;
import ir.daneshrefah.scm.uaa.service.otp.dto.*;
import ir.daneshrefah.scm.uaa.service.user.UserService;
import ir.daneshrefah.scm.uaa.utils.ProfileInfo;
import ir.daneshrefah.scm.utils.string.StringUtils;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Objects;

import static ir.daneshrefah.scm.common.constant.CacheConstants.CACHE_NAME_OTP;
import static ir.daneshrefah.scm.uaa.utils.RequestUtils.extractRequestAccessParameter;
import static ir.daneshrefah.scm.uaa.utils.RequestUtils.extractRequestTerminalCode;

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

    public SmsOtpProvider(
            CacheTemplate cacheTemplate,
            OtpProperties otpProperties,
            NotificationService notificationService,
            ProfileInfo profileInfo,
            @Lazy UserService userService) {
        super(cacheTemplate, otpProperties, profileInfo, userService);
        this.notificationService = notificationService;
    }

    @Override
    public OtpSendResponse sendOtpInternal(OtpSendRequest request) {
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
        ValidationUtils.checkInvalidMobileNumber(mobileNumber, () -> new InvalidInputException("mobile"));
    }

    private void sendNotification(Otp otp) {
        NotificationData data = new NotificationData();
        data.put(NotificationDataKey.OTP_CODE, otp.getOtpCode());
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
        return OtpVerifyResponse.builder()
                .isSuccessful(true)
                .build();
    }

    @Override
    public OtpVerifyResponse verifyOtpByLoggedInUser(VerifyOtpByLoggedInUserRequest request) {
        UserEntity userEntity = userService.findUser(Objects.requireNonNull(AuthenticationUtils.getLoggedInUserAuthentication()));
        GeneralPersonEntity personEntity = userEntity.getPerson();
        return verifyOtpForUser(
                request,
                personEntity.getMobile1(),
                userEntity.getNickname(),
                UserIdentifierType.USER_NICKNAME,
                userEntity,
                request.getOtpAuthenticationType());
    }

    @Override
    public OtpVerifyResponse verifyOtpByDelegatedUser(VerifyOtpByDelegatedUserRequest request) {
        ValidationUtils.checkNull(request.getOtpType(), () -> new MissingRequiredInputException("otpType"));
        ValidationUtils.checkNull(request.getReason(), () -> new MissingRequiredInputException("reason"));
        ValidationUtils.checkNull(request.getRecipient(), () -> new MissingRequiredInputException("recipient"));
        String terminalCode = extractRequestTerminalCode();
        String accessParameter = extractAccessParameterOrThrow();
        Recipient recipient = createRecipient(request.getRecipient(), request.getRecipientId(), request.getRecipientIdType(), terminalCode, accessParameter);
        OtpVerifyRequest otpVerifyRequest = createOtpVerifyRequest(request.getOtpType(), recipient, request.getReason(), request.getClaimCode());
        return verifyOtp(otpVerifyRequest);
    }

    @Override
    public OtpVerifyResponse verifyOtpByUsername(VerifyOtpByUsernameRequest request) {
        GeneralPersonEntity generalPerson = userService.findPersonByUsername(request.getUsername());
        UserEntity userEntity = findUserByPersonAndTerminal(generalPerson.getId(), extractRequestTerminalCode());
        return verifyOtpForUser(
                request,
                generalPerson.getMobile1(),
                generalPerson.getUsername(),
                UserIdentifierType.PERSON_USERNAME,
                userEntity,
                request.getOtpAuthenticationType());
    }

    @Override
    public OtpVerifyResponse verifyOtpByNickname(VerifyOtpByNicknameRequest request) {
        UserEntity userEntity = findUserByNicknameAndTerminal(request.getNickname(), extractRequestTerminalCode());
        GeneralPersonEntity generalPerson = userEntity.getPerson();
        return verifyOtpForUser(
                request,
                generalPerson.getMobile1(),
                userEntity.getNickname(),
                UserIdentifierType.USER_NICKNAME,
                userEntity,
                request.getOtpAuthenticationType());
    }

    private String extractAccessParameterOrThrow() {
        return extractRequestAccessParameter().orElseThrow(() -> new MissingRequiredInputException("accessParameter"));
    }

    private OtpVerifyResponse handleStaticPasswordAuthentication(String claimCode, UserEntity userEntity, OtpAuthenticationType otpAuthenticationType) {
        boolean isValid = userService.validateStaticPassword(userEntity, claimCode, otpAuthenticationType);
        return OtpVerifyResponse.builder()
                .isSuccessful(isValid)
                .build();
    }

    private OtpVerifyResponse verifyOtpForUser(VerifyOTP request,
                                               String mobile,
                                               String identifier,
                                               UserIdentifierType identifierType,
                                               UserEntity userEntity,
                                               OtpAuthenticationType otpAuthenticationType) {
        String terminalCode = extractRequestTerminalCode();
        String accessParameter = extractAccessParameterOrThrow();
        AuthenticationMethod authenticationMethod = null;
        if (otpAuthenticationType.equals(OtpAuthenticationType.TRANSACTION)) {
            authenticationMethod = userEntity.getTransactionAuthenticationMethod();
        } else if (otpAuthenticationType.equals(OtpAuthenticationType.LOGIN)) {
            authenticationMethod = userEntity.getLoginAuthenticationMethod();
        }
        switch (authenticationMethod) {
            case STATIC_PASSWORD:
                return handleStaticPasswordAuthentication(request.getClaimCode(), userEntity, otpAuthenticationType);
            case SMS:
                Recipient recipient = createRecipient(mobile, identifier, identifierType, terminalCode, accessParameter);
                OtpVerifyRequest otpVerifyRequest = createOtpVerifyRequest(request.getOtpType(), recipient, request.getReason(), request.getClaimCode());
                return verifyOtp(otpVerifyRequest);
            case OTP:
                throw new UnsupportedOperationException("Unsupported authentication method: " + userEntity.getLoginAuthenticationMethod());
            default:
                throw new UnsupportedOperationException("Unsupported authentication method: " + userEntity.getLoginAuthenticationMethod());
        }
    }

    private Recipient createRecipient(String address, String identifier, UserIdentifierType identifierType, String terminalCode, String accessParameter) {
        return Recipient.builder()
                .address(address)
                .identifier(identifier)
                .identifierType(identifierType)
                .terminalCode(terminalCode)
                .accessParameter(accessParameter)
                .build();
    }

    private OtpVerifyRequest createOtpVerifyRequest(OtpType otpType, Recipient recipient, OtpReason reason, String claimCode) {
        return OtpVerifyRequest.builder()
                .otpType(otpType)
                .recipient(recipient)
                .reason(reason)
                .claimCode(claimCode)
                .build();
    }

    private UserEntity findUserByPersonAndTerminal(Integer personId, String terminalCode) {
        return userService.findByPersonIdAndLegacyTerminalCode(personId, terminalCode).stream()
                .findFirst().orElseThrow(() -> new NoMatchRecordFoundException("userId"));
    }

    private UserEntity findUserByNicknameAndTerminal(String nickname, String terminalCode) {
        return userService.findByNicknameAndLegacyTerminalCode(nickname, terminalCode).stream()
                .findFirst().orElseThrow(() -> new NoMatchRecordFoundException("userId"));
    }

    @Override
    public OtpType getType() {
        return OtpType.SMS;
    }
}

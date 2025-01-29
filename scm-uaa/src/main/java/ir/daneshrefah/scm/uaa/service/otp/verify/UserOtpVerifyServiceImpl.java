package ir.daneshrefah.scm.uaa.service.otp.verify;

import ir.daneshrefah.scm.common.constant.otp.OtpReason;
import ir.daneshrefah.scm.common.constant.otp.OtpType;
import ir.daneshrefah.scm.common.data.entity.person.GeneralPersonEntity;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.model.recipient.Recipient;
import ir.daneshrefah.scm.common.model.user.AuthenticationMethod;
import ir.daneshrefah.scm.common.model.user.UserIdentifierType;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.uaa.domain.otp.AuthenticationMethodType;
import ir.daneshrefah.scm.uaa.repository.authentication.UserEntity;
import ir.daneshrefah.scm.uaa.service.otp.OtpService;
import ir.daneshrefah.scm.uaa.service.otp.dto.*;
import ir.daneshrefah.scm.uaa.service.user.UserService;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static ir.daneshrefah.scm.uaa.utils.RequestUtils.extractRequestAccessParameter;
import static ir.daneshrefah.scm.uaa.utils.RequestUtils.extractRequestTerminalCode;

@Service
@RequiredArgsConstructor
public class UserOtpVerifyServiceImpl implements UserOtpVerifyService {

    private final OtpService otpService;
    private final UserService userService;

    private void validateOtpRequest(OtpType otpType, String identifier, String claimCode, OtpReason reason, String identifierName) {
        ValidationUtils.checkNull(otpType, () -> new MissingRequiredInputException("otpType"));
        ValidationUtils.checkBlankString(identifier, () -> new MissingRequiredInputException(identifierName));
        ValidationUtils.checkBlankString(claimCode, () -> new MissingRequiredInputException("claimCode"));
        ValidationUtils.checkNull(reason, () -> new MissingRequiredInputException("reason"));
    }

    @Override
    public OtpVerifyResponse verifyOtpByLoggedInUser(VerifyOtpByLoggedInUserRequest request) {
        ValidationUtils.checkNull(request.getOtpType(), () -> new MissingRequiredInputException("otpType"));
        ValidationUtils.checkBlankString(request.getClaimCode(), () -> new MissingRequiredInputException("claimCode"));
        ValidationUtils.checkNull(request.getReason(), () -> new MissingRequiredInputException("reason"));
        UserEntity userEntity = userService.findUser(Objects.requireNonNull(AuthenticationUtils.getLoggedInUserAuthentication()));
        GeneralPersonEntity personEntity = userEntity.getPerson();
        return verifyOtpForUser(
                request,
                personEntity.getMobile1(),
                userEntity.getNickname(),
                UserIdentifierType.USER_NICKNAME,
                userEntity,
                request.getAuthenticationMethodType());
    }

    @Override
    public OtpVerifyResponse verifyOtpByDelegatedUser(VerifyOtpByDelegatedUserRequest request) {
        ValidationUtils.checkNull(request.getOtpType(), () -> new MissingRequiredInputException("otpType"));
        ValidationUtils.checkNull(request.getReason(), () -> new MissingRequiredInputException("reason"));
        ValidationUtils.checkNull(request.getRecipient(), () -> new MissingRequiredInputException("recipient"));
        String terminalCode = extractRequestTerminalCode();
        String accessParameter = extractAccessParameterOrThrow();
        Recipient recipient = createRecipient(request.getRecipient(), request.getRecipientId(), request.getRecipientIdType(), terminalCode, accessParameter);
        OtpVerifyRequest otpVerifyRequest = createOtpVerifyRequest(request.getOtpType(), recipient, request.getReason(), request.getClaimCode(), null);
        return otpService.verifyOtp(otpVerifyRequest);
    }

    @Override
    public OtpVerifyResponse verifyOtpByUsername(VerifyOtpByUsernameRequest request) {
        validateOtpRequest(request.getOtpType(), request.getUsername(), request.getClaimCode(), request.getReason(), "Username");
        GeneralPersonEntity generalPerson = userService.findPersonByUsername(request.getUsername());
        UserEntity userEntity = findUserByPersonAndTerminal(generalPerson.getId(), extractRequestTerminalCode());
        return verifyOtpForUser(
                request,
                generalPerson.getMobile1(),
                generalPerson.getUsername(),
                UserIdentifierType.PERSON_USERNAME,
                userEntity,
                request.getAuthenticationMethodType());
    }

    @Override
    public OtpVerifyResponse verifyOtpByNickname(VerifyOtpByNicknameRequest request) {
        validateOtpRequest(request.getOtpType(), request.getNickname(), request.getClaimCode(), request.getReason(), "nickname");
        UserEntity userEntity = findUserByNicknameAndTerminal(request.getNickname(), extractRequestTerminalCode());
        GeneralPersonEntity generalPerson = userEntity.getPerson();
        return verifyOtpForUser(
                request,
                generalPerson.getMobile1(),
                userEntity.getNickname(),
                UserIdentifierType.USER_NICKNAME,
                userEntity,
                request.getAuthenticationMethodType());
    }

    @Override
    public OtpVerifyResponse verifyOtpByNationalCode(VerifyOtpByNationalCodeRequest request) {
        validateOtpRequest(request.getOtpType(), request.getNationalCode(), request.getClaimCode(), request.getReason(), "nationalCode");
        UserEntity userEntity = userService.findByNationalCodeAndTerminalIDAndSubOrganizationId(request.getNationalCode(), request.getSubOrganizationId(), extractRequestTerminalCode()).orElseThrow(() -> {
            throw new NoMatchRecordFoundException("user");});
        GeneralPersonEntity generalPerson = userEntity.getPerson();
        return verifyOtpForUser(
                request,
                generalPerson.getMobile1(),
                userEntity.getNickname(),
                UserIdentifierType.PERSON_USERNAME,
                userEntity,
                request.getAuthenticationMethodType());
    }

    private UserEntity findUserByPersonAndTerminal(Long personId, String terminalCode) {
        return userService.findByPersonIdAndLegacyTerminalCode(personId, terminalCode).stream()
                .findFirst().orElseThrow(() -> new NoMatchRecordFoundException("userId"));
    }

    private UserEntity findUserByNicknameAndTerminal(String nickname, String terminalCode) {
        return userService.findByNicknameAndLegacyTerminalCode(nickname, terminalCode).stream()
                .findFirst().orElseThrow(() -> new NoMatchRecordFoundException("userId"));
    }

    private String extractAccessParameterOrThrow() {
        return extractRequestAccessParameter().orElseThrow(() -> new MissingRequiredInputException("accessParameter"));
    }

    private OtpVerifyResponse handleStaticPasswordAuthentication(String claimCode, UserEntity userEntity, AuthenticationMethodType authenticationMethodType) {
        boolean isValid = userService.validateStaticPassword(userEntity, claimCode, authenticationMethodType);
        return OtpVerifyResponse.builder()
                .isSuccessful(isValid)
                .build();
    }

    private OtpVerifyResponse verifyOtpForUser(VerifyOTP request,
                                               String mobile,
                                               String identifier,
                                               UserIdentifierType identifierType,
                                               UserEntity userEntity,
                                               AuthenticationMethodType authenticationMethodType) {
        String terminalCode = extractRequestTerminalCode();
        String accessParameter = extractAccessParameterOrThrow();
        AuthenticationMethod authenticationMethod = null;
        if (authenticationMethodType.equals(AuthenticationMethodType.TRANSACTION)) {
            authenticationMethod = userEntity.getTransactionAuthenticationMethod();
        } else if (authenticationMethodType.equals(AuthenticationMethodType.LOGIN)) {
            authenticationMethod = userEntity.getLoginAuthenticationMethod();
        }
        switch (authenticationMethod) {
            case STATIC_PASSWORD:
                return handleStaticPasswordAuthentication(request.getClaimCode(), userEntity, authenticationMethodType);
            case SMS, OTP:
                Recipient recipient = createRecipient(mobile, identifier, identifierType, terminalCode, accessParameter);
                OtpVerifyRequest otpVerifyRequest = createOtpVerifyRequest(request.getOtpType(), recipient, request.getReason(), request.getClaimCode(), userEntity);
                return otpService.verifyOtp(otpVerifyRequest);
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

    private OtpVerifyRequest createOtpVerifyRequest(OtpType otpType, Recipient recipient, OtpReason reason, String claimCode, UserEntity userEntity) {
        return OtpVerifyRequest.builder()
                .otpType(otpType)
                .recipient(recipient)
                .reason(reason)
                .userEntity(userEntity)
                .claimCode(claimCode)
                .build();
    }
}
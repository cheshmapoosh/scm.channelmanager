package ir.daneshrefah.scm.uaa.service.otp;

import ir.daneshrefah.scm.common.constant.otp.OtpReason;
import ir.daneshrefah.scm.common.constant.otp.OtpType;
import ir.daneshrefah.scm.common.data.entity.person.GeneralPersonEntity;
import ir.daneshrefah.scm.common.exception.AuthenticationRequiredException;
import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.model.recipient.Recipient;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.common.model.user.UserIdentifierType;
import ir.daneshrefah.scm.common.dto.terminal.TerminalService;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.uaa.controller.otp.DelegatedSmsOtpSendRequest;
import ir.daneshrefah.scm.uaa.controller.otp.SmsOtpSendRequest;
import ir.daneshrefah.scm.uaa.exception.BaseOtpException;
import ir.daneshrefah.scm.uaa.repository.authentication.UserEntity;
import ir.daneshrefah.scm.uaa.service.otp.dto.*;
import ir.daneshrefah.scm.uaa.service.otp.provder.AbstractOtpProvider;
import ir.daneshrefah.scm.uaa.service.user.UserService;
import ir.daneshrefah.scm.uaa.utils.ProfileInfo;
import ir.daneshrefah.scm.utils.string.StringUtils;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ir.daneshrefah.scm.uaa.utils.RequestUtils.extractRequestAccessParameter;
import static ir.daneshrefah.scm.uaa.utils.RequestUtils.extractRequestTerminalCode;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-30
 */
@Service
public class OtpService {

    private final TerminalService terminalService;
    private final Map<OtpType, AbstractOtpProvider> providers;
    private final ProfileInfo profileInfo;
    private final UserService userService;

    public OtpService(List<AbstractOtpProvider> providers, TerminalService terminalService, ProfileInfo profileInfo, @Lazy UserService userService) {
        this.terminalService = terminalService;
        this.profileInfo = profileInfo;
        this.providers = providers.stream()
                .collect(Collectors.toMap(AbstractOtpProvider::getType, Function.identity()));

        this.userService = userService;
    }

    public OtpSendResponse sendOtp(OtpSendRequest request) {
        ValidationUtils.checkNull(request.getOtpType(), () -> new MissingRequiredInputException("otpType"));
        ValidationUtils.checkNull(request.getReason(), () -> new MissingRequiredInputException("reason"));
        ValidationUtils.checkNull(request.getRecipient(), () -> new MissingRequiredInputException("recipient"));
        ValidationUtils.checkBlankString(request.getRecipient().getAddress(), () -> new MissingRequiredInputException("recipient.address"));
        ValidationUtils.checkBlankString(request.getRecipient().getIdentifier(), () -> new MissingRequiredInputException("recipient.identifier"));
        ValidationUtils.checkNull(request.getRecipient().getIdentifierType(), () -> new MissingRequiredInputException("recipient.identifierType"));
        ValidationUtils.checkBlankString(request.getRecipient().getTerminalCode(), () -> new MissingRequiredInputException("recipient.terminalCode"));
        ValidationUtils.checkBlankString(request.getRecipient().getAccessParameter(), () -> new MissingRequiredInputException("recipient.accessParameter"));
        Optional<Terminal> terminal = terminalService.findTerminalByCode(request.getRecipient().getTerminalCode());
        ValidationUtils.checkEmptyOptional(terminal, () -> new InvalidInputException("terminalCode"));
        AbstractOtpProvider provider = getValidOtpProvider(request.getOtpType());
        if (Objects.isNull(provider)) {
            return createInvalidResponse(request, "Unsupported otpType.");
        }
        return provider.sendOtp(request);
    }

    private OtpSendResponse createInvalidResponse(OtpSendRequest request, String errorMessage) {
        Otp otp = Otp.builder()
                .otpType(request.getOtpType())
                .reason(request.getReason())
                .recipient(request.getRecipient())
                .build();
        return OtpSendResponse.builder()
                .otp(otp)
                .isSuccessful(false)
                .errorMessage(errorMessage)
                .build();
    }

    private OtpVerifyResponse createInvalidVerifyResponse(String errorMessage) {
        return OtpVerifyResponse.builder()
                .isSuccessful(false)
                .errorMessage(errorMessage)
                .build();
    }

    public OtpSendResponse sendOtpByLoggedInUser(SmsOtpSendRequest request) {
        User user = AuthenticationUtils.getLoggedInUser();
        ValidationUtils.checkNull(user, AuthenticationRequiredException::new);
        String terminalCode = extractRequestTerminalCode();
        String accessParameter = extractRequestAccessParameter().orElseThrow(() -> new MissingRequiredInputException("accessParameter"));
        assert user != null;
        GeneralPersonEntity personEntity = userService.findPersonById(user.getPerson().getId());
        Recipient recipient = Recipient.builder()
                .address(personEntity.getMobile1())
                .identifier(user.getNickname())
                .identifierType(UserIdentifierType.USER_NICKNAME)
                .terminalCode(terminalCode)
                .accessParameter(accessParameter)
                .build();
        OtpSendRequest otpRequest = OtpSendRequest.builder()
                .otpType(OtpType.SMS)
                .reason(request.getReason())
                .recipient(recipient)
                .build();
        return sendOtp(otpRequest);
    }

    public OtpSendResponse sendOtpByDelegated(DelegatedSmsOtpSendRequest request) {
        String terminalCode = extractRequestTerminalCode();
        String accessParameter = extractRequestAccessParameter().orElseThrow(() -> new MissingRequiredInputException("accessParameter"));
        Recipient recipient = Recipient.builder()
                .address(request.getRecipient())
                .identifier(StringUtils.isNotBlank(request.getRecipientId()) ? request.getRecipientId() : request.getRecipient())
                .identifierType(Objects.nonNull(request.getRecipientIdType()) ? request.getRecipientIdType() : UserIdentifierType.MOBILE_NUMBER)
                .terminalCode(StringUtils.isNotBlank(request.getTerminalCode()) ? request.getTerminalCode() : terminalCode)
                .accessParameter(StringUtils.isNotBlank(request.getAccessParameter()) ? request.getAccessParameter() : accessParameter)
                .build();
        OtpSendRequest otpRequest = OtpSendRequest.builder()
                .otpType(OtpType.SMS)
                .reason(request.getReason())
                .recipient(recipient)
                .build();
        return sendOtp(otpRequest);
    }

    public OtpSendResponse sendOtpByAddress(String recipientAddress) {
        String terminalCode = extractRequestTerminalCode();
        String accessParameter = extractRequestAccessParameter().orElseThrow(() -> new MissingRequiredInputException("accessParameter"));
        Recipient recipient = Recipient.builder()
                .address(recipientAddress)
                .identifier(recipientAddress)
                .identifierType(UserIdentifierType.MOBILE_NUMBER)
                .terminalCode(terminalCode)
                .accessParameter(accessParameter)
                .build();
        OtpSendRequest otpRequest = OtpSendRequest.builder()
                .otpType(OtpType.SMS)
                .reason(OtpReason.AUTHENTICATION)
                .recipient(recipient)
                .build();
        return sendOtp(otpRequest);
    }

    public OtpSendResponse sendOtpByUsername(OtpSmsBasedUsernameRequest request) {
        String terminalCode = extractRequestTerminalCode();
        String accessParameter = extractRequestAccessParameter().orElseThrow(() -> new MissingRequiredInputException("accessParameter"));
        GeneralPersonEntity generalPerson = userService.findPersonByUsername(request.getUsername());
        Recipient recipient = Recipient.builder()
                .address(generalPerson.getMobile1())
                .identifier(generalPerson.getUsername())
                .identifierType(UserIdentifierType.PERSON_USERNAME)
                .terminalCode(terminalCode)
                .accessParameter(accessParameter)
                .build();
        OtpSendRequest otpRequest = OtpSendRequest.builder()
                .otpType(OtpType.SMS)
                .reason(request.getReason())
                .recipient(recipient)
                .build();
        return sendOtp(otpRequest);
    }

    public OtpSendResponse sendOtpByNickname(OtpSmsBasedNicknameRequest request) {
        ValidationUtils.checkNull(request.getNickname(), () -> new InvalidInputException("nickname("));
        String terminalCode = extractRequestTerminalCode();
        String accessParameter = extractRequestAccessParameter().orElseThrow(() -> new MissingRequiredInputException("accessParameter"));
        UserEntity userEntity = userService.findByNicknameAndLegacyTerminalCode(request.getNickname(), terminalCode).stream()
                .findFirst()
                .orElseThrow(() -> new NoMatchRecordFoundException("nickname"));
        GeneralPersonEntity generalPersonEntity = userEntity.getPerson();
        Recipient recipient = Recipient.builder()
                .address(generalPersonEntity.getMobile1())
                .identifier(userEntity.getNickname())
                .identifierType(UserIdentifierType.USER_NICKNAME)
                .terminalCode(terminalCode)
                .accessParameter(accessParameter)
                .build();
        OtpSendRequest otpRequest = OtpSendRequest.builder()
                .otpType(OtpType.SMS)
                .reason(request.getReason())
                .recipient(recipient)
                .build();
        return sendOtp(otpRequest);
    }

    public OtpVerifyResponse verifyOtp(OtpVerifyRequest request) {
        AbstractOtpProvider provider = providers.get(request.getOtpType());
        try {
            return provider.verifyOtp(request);
        } catch (BaseOtpException e) {
            return createInvalidVerifyResponse(e.getMessage());
        }
    }

    public OtpVerifyResponse verifyOtpByLoggedInUser(VerifyOtpByLoggedInUserRequest request) {
        ValidationUtils.checkNull(request.getOtpType(), () -> new MissingRequiredInputException("otpType"));
        ValidationUtils.checkBlankString(request.getClaimCode(), () -> new MissingRequiredInputException("claimCode"));
        ValidationUtils.checkNull(request.getReason(), () -> new MissingRequiredInputException("reason"));
        return verifyOtp(request, provider -> provider.verifyOtpByLoggedInUser(request));
    }

    public OtpVerifyResponse verifyOtpByDelegatedUser(VerifyOtpByDelegatedUserRequest request) {
        return verifyOtp(request, provider -> provider.verifyOtpByDelegatedUser(request));
    }

    public OtpVerifyResponse verifyOtpByUsername(VerifyOtpByUsernameRequest request) {
        validateOtpRequest(request.getOtpType(), request.getUsername(), request.getClaimCode(), request.getReason(), "Username");
        return verifyOtp(request, provider -> provider.verifyOtpByUsername(request));
    }

    public OtpVerifyResponse verifyOtpByNickname(VerifyOtpByNicknameRequest request) {
        validateOtpRequest(request.getOtpType(), request.getNickname(), request.getClaimCode(), request.getReason(), "nickname");
        return verifyOtp(request, provider -> provider.verifyOtpByNickname(request));
    }

    private OtpVerifyResponse verifyOtp(VerifyOTP request, Function<AbstractOtpProvider, OtpVerifyResponse> verifyFunction) {
        AbstractOtpProvider provider = getValidOtpProvider(request.getOtpType());
        try {
            return verifyFunction.apply(provider);
        } catch (BaseOtpException e) {
            return createInvalidVerifyResponse(e.getMessage());
        }
    }

    public AbstractOtpProvider getValidOtpProvider(OtpType optType) {
        ValidationUtils.checkNull(optType, () -> new InvalidInputException("otpType"));
        AbstractOtpProvider provider = providers.get(optType);
        if (Objects.isNull(provider)) {
            throw new UnsupportedOperationException();
        }
        return provider;
    }

    private void validateOtpRequest(OtpType otpType, String identifier, String claimCode, OtpReason reason, String identifierName) {
        ValidationUtils.checkNull(otpType, () -> new MissingRequiredInputException("otpType"));
        ValidationUtils.checkBlankString(identifier, () -> new MissingRequiredInputException(identifierName));
        ValidationUtils.checkBlankString(claimCode, () -> new MissingRequiredInputException("claimCode"));
        ValidationUtils.checkNull(reason, () -> new MissingRequiredInputException("reason"));
    }
}

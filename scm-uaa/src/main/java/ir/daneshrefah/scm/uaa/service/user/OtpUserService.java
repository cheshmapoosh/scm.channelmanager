package ir.daneshrefah.scm.uaa.service.user;

import ir.daneshrefah.scm.common.constant.otp.OtpReason;
import ir.daneshrefah.scm.common.constant.otp.OtpType;
import ir.daneshrefah.scm.common.data.entity.person.GeneralPersonEntity;
import ir.daneshrefah.scm.common.data.service.person.PersonService;
import ir.daneshrefah.scm.common.exception.AuthenticationRequiredException;
import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.common.model.recipient.Recipient;
import ir.daneshrefah.scm.common.model.user.UserIdentifierType;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.uaa.controller.otp.DelegatedSmsOtpSendRequest;
import ir.daneshrefah.scm.uaa.controller.otp.SmsOtpSendRequest;
import ir.daneshrefah.scm.uaa.repository.authentication.UserEntity;
import ir.daneshrefah.scm.uaa.service.otp.OtpService;
import ir.daneshrefah.scm.uaa.service.otp.dto.*;
import ir.daneshrefah.scm.utils.string.StringUtils;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static ir.daneshrefah.scm.uaa.utils.RequestUtils.extractRequestAccessParameter;
import static ir.daneshrefah.scm.uaa.utils.RequestUtils.extractRequestTerminalCode;

@RequiredArgsConstructor
@Service
public class OtpUserService {

    private final OtpService otpService;
    private final UserService userService;
    private final PersonService personService;

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
        return otpService.sendOtp(otpRequest);
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
                .metadata(request.getMetadata())
                .build();
        return otpService.sendOtp(otpRequest);
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
        return otpService.sendOtp(otpRequest);
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
        return otpService.sendOtp(otpRequest);
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
        return otpService.sendOtp(otpRequest);
    }

    public OtpSendResponse sendOtpSmsByNationalId(OtpSmsBasedNationalCodeRequest request) {
        String terminalCode = extractRequestTerminalCode();
        String accessParameter = extractRequestAccessParameter().orElseThrow(() -> new MissingRequiredInputException("accessParameter"));
        GeneralPerson generalPerson = personService
                .findPerson(request.getPersonType(),request.getNationalId(),request.getSubOrg())
                .orElseThrow(()->new NoMatchRecordFoundException("user"));
        Recipient recipient = Recipient.builder()
                .address(generalPerson.getMobile1())
                .identifier(generalPerson.getMobile1())
                .identifierType(UserIdentifierType.MOBILE_NUMBER)
                .terminalCode(terminalCode)
                .accessParameter(accessParameter)
                .build();
        OtpSendRequest otpRequest = OtpSendRequest.builder()
                .otpType(OtpType.SMS)
                .reason(request.getReason())
                .recipient(recipient)
                .build();
        return otpService.sendOtp(otpRequest);
    }
}

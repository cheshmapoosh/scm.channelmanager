package ir.daneshrefah.scm.uaa.service.register;

import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.common.exception.MethodNotSupportDataException;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.model.notification.constants.NotificationMedia;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.domain.client.Client;
import ir.daneshrefah.scm.uaa.domain.client.ClientVersion;
import ir.daneshrefah.scm.uaa.domain.otp.OtpReason;
import ir.daneshrefah.scm.uaa.domain.otp.OtpType;
import ir.daneshrefah.scm.uaa.repository.activation.UserActivationEntity;
import ir.daneshrefah.scm.uaa.repository.activation.UserActivationRepository;
import ir.daneshrefah.scm.uaa.service.ClientService;
import ir.daneshrefah.scm.uaa.service.otp.OtpService;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpSendRequest;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpSendResponse;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpVerifyRequest;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpVerifyResponse;
import ir.daneshrefah.scm.uaa.service.user.UserService;
import ir.daneshrefah.scm.uaa.utils.UserValidationWrapper;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-18
 */
@RequiredArgsConstructor
@Service
public class RegisterService {

    private final UserActivationRepository userActivationRepository;
    private final OtpService otpService;
    private final ClientService clientService;
    private final UserService userService;

    public SubmitRegisterResponse submitUserRegister(String username, String address, SubmitRegisterRequest request) {
        if (StringUtils.isEmpty(username)) {
            throw new MissingRequiredInputException("username");
        }
        if (null == request) {
            throw new MissingRequiredInputException("request");
        }
        if (StringUtils.isEmpty(request.getClientId())) {
            throw new MissingRequiredInputException("clientId");
        }
        if (StringUtils.isEmpty(request.getRecipient())) {
            throw new MissingRequiredInputException("recipient");
        }
        Client client = clientService.findByClientId(request.getClientId()).orElseThrow(() -> new InvalidInputException("clientId"));
        if (!client.isCheckActivation()) {
            throw new MethodNotSupportDataException("checkActivation");
        }
        if (client.isCheckVersion()) {
            if (StringUtils.isEmpty(request.getClientVersion())) {
                throw new MissingRequiredInputException("clientVersion");
            }
            Optional<ClientVersion> clientVersion = client.getVersions().stream().filter(version -> request.getClientVersion().equals(version.getVersion())).findFirst();
            if (clientVersion.isEmpty()) {
                throw new InvalidInputException("clientVersion");
            }
            if (StringUtils.isNotEmpty(clientVersion.get().getSignature()) &&
                    !clientVersion.get().getSignature().equals(request.getClientSignature())) {
                throw new InvalidInputException("clientSignature");
            }
        }
        Optional<User> user = userService.loadUserByUsername(username, client.getTerminalCode());
        if (user.isEmpty()) {
            throw new InvalidInputException("username");
        }
        UserValidationWrapper validator = new UserValidationWrapper(user.get());
        if (!validator.containsMobile(request.getRecipient())) {
            throw new InvalidInputException("recipient");
        }
        OtpSendRequest otpRequest = OtpSendRequest.builder()
                .terminalCode(client.getTerminalCode())
                .accessParameter(null)
                .recipientUsername(username)
                .recipient(request.getRecipient())
                .otpType(OtpType.SMS)
                .reason(OtpReason.ACTIVATION)
                .issuerAddress(address)
                .issuerUsername(username)
                .build();
        OtpSendResponse otpResponse = otpService.sendOtp(otpRequest, null);
        return SubmitRegisterResponse.builder()
                .clientId(request.getClientId())
                .recipient(request.getRecipient())
                .media(NotificationMedia.SMS)
                .isSuccessful(otpResponse.isSuccessful())
                .expireTime(otpResponse.getExpireTime())
                .build();
    }

    public ConfirmRegisterResponse confirmUserRegister(String username, ConfirmRegisterRequest request) {
        if (StringUtils.isEmpty(username)) {
            throw new MissingRequiredInputException("username");
        }
        if (null == request) {
            throw new MissingRequiredInputException("request");
        }
        if (StringUtils.isEmpty(request.getClientId())) {
            throw new MissingRequiredInputException("clientId");
        }
        if (StringUtils.isEmpty(request.getRecipient())) {
            throw new MissingRequiredInputException("recipient");
        }
        if (StringUtils.isEmpty(request.getClaimCode())) {
            throw new MissingRequiredInputException("claimCode");
        }
        Client client = clientService.findByClientId(request.getClientId()).orElseThrow(() -> new InvalidInputException("clientId"));
        if (!client.isCheckActivation()) {
            throw new MethodNotSupportDataException("checkActivation");
        }
        if (client.isCheckVersion()) {
            if (StringUtils.isEmpty(request.getClientVersion())) {
                throw new MissingRequiredInputException("clientVersion");
            }
            Optional<ClientVersion> clientVersion = client.getVersions().stream().filter(version -> request.getClientVersion().equals(version.getVersion())).findFirst();
            if (clientVersion.isEmpty()) {
                throw new InvalidInputException("clientVersion");
            }
            if (StringUtils.isNotEmpty(clientVersion.get().getSignature()) &&
                    !clientVersion.get().getSignature().equals(request.getClientSignature())) {
                throw new InvalidInputException("clientSignature");
            }
        }
//        Optional<User> user = userService.loadUserByUsername(username, client.getTerminalCode());
//        if (user.isEmpty()) {
//            throw new InvalidInputException("username");
//        }
//        UserValidationWrapper validator = new UserValidationWrapper(user.get());
//        if (!validator.containsMobile(request.getRecipient())) {
//            throw new InvalidInputException("recipient");
//        }
        OtpVerifyRequest otpRequest = OtpVerifyRequest.builder()
                .terminalCode(client.getTerminalCode())
                .accessParameter(null)
                .recipientUsername(username)
                .recipient(request.getRecipient())
                .otpType(OtpType.SMS)
                .reason(OtpReason.ACTIVATION)
                .claimCode(request.getClaimCode())
                .build();
        OtpVerifyResponse otpResponse = otpService.verifyOtp(otpRequest);
        if (!otpResponse.isSuccessful()) {
            throw new InvalidInputException("claimCode");
        }

        String activationCode = StringUtils.generateGuid();
        UserActivationEntity activationEntity = new UserActivationEntity();
        activationEntity.setUsername(username);
        activationEntity.setAccessParameter(null); //TODO
        activationEntity.setTerminalCode(client.getTerminalCode());
        activationEntity.setActivationCode(activationCode);
        activationEntity.setActivated(true);
        activationEntity.setAgent(null);
        activationEntity.setDeviceModel(null);
        activationEntity.setOsVersion(null);
        activationEntity.setClientId(client.getClientId()); //TODO
        activationEntity.setTokenSetTime(LocalDateTime.now()); //TODO
//        private LocalDateTime lastUsed;
        userActivationRepository.save(activationEntity);

        return ConfirmRegisterResponse.builder()
                .isSuccessful(otpResponse.isSuccessful())
                .activationCode(activationCode)
                .build();
    }

}

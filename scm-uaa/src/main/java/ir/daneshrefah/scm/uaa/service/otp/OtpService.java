package ir.daneshrefah.scm.uaa.service.otp;

import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.common.service.terminal.TerminalService;
import ir.daneshrefah.scm.uaa.domain.otp.OtpType;
import ir.daneshrefah.scm.uaa.exception.BaseOtpException;
import ir.daneshrefah.scm.uaa.service.otp.dto.*;
import ir.daneshrefah.scm.uaa.service.otp.provder.AbstractOtpProvider;
import ir.daneshrefah.scm.uaa.utils.ProfileInfo;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    public OtpService(List<AbstractOtpProvider> providers, TerminalService terminalService, ProfileInfo profileInfo) {
        this.terminalService = terminalService;
        this.profileInfo = profileInfo;
        this.providers = providers.stream()
                .collect(Collectors.toMap(AbstractOtpProvider::getType, Function.identity()));
    }

    public OtpSendResponse sendOtp(OtpSendRequest request) {
        ValidationUtils.checkNull(request.getOtpType(), () -> new MissingRequiredInputException("otpType"));
        ValidationUtils.checkNull(request.getReason(), () -> new MissingRequiredInputException("reason"));

        ValidationUtils.checkNull(request.getRecipient(), () -> new MissingRequiredInputException("recipient"));
        ValidationUtils.checkBlankString(request.getRecipient().getAddress(), () -> new MissingRequiredInputException("recipient.address"));
//        ValidationUtils.checkNull(request.getRecipient().getAuthenticationLevel(), () -> new MissingRequiredInputException("recipient.authenticationLevel"));
        ValidationUtils.checkBlankString(request.getRecipient().getIdentifier(), () -> new MissingRequiredInputException("recipient.identifier"));
        ValidationUtils.checkNull(request.getRecipient().getIdentifierType(), () -> new MissingRequiredInputException("recipient.identifierType"));
        ValidationUtils.checkBlankString(request.getRecipient().getTerminalCode(), () -> new MissingRequiredInputException("recipient.terminalCode"));
        ValidationUtils.checkBlankString(request.getRecipient().getAccessParameter(), () -> new MissingRequiredInputException("recipient.accessParameter"));

//        ValidationUtils.checkNull(request.getIssuer(), () -> new MissingRequiredInputException("issuer"));
//        ValidationUtils.checkNull(request.getIssuer().getAuthenticationLevel(), () -> new MissingRequiredInputException("issuer.authenticationLevel"));
//        ValidationUtils.checkBlankString(request.getIssuer().getIdentifier(), () -> new MissingRequiredInputException("issuer.identifier"));
//        ValidationUtils.checkNull(request.getIssuer().getIdentifierType(), () -> new MissingRequiredInputException("issuer.identifierType"));
//        ValidationUtils.checkBlankString(request.getIssuer().getTerminalCode(), () -> new MissingRequiredInputException("issuer.terminalCode"));
//        ValidationUtils.checkBlankString(request.getIssuer().getAccessParameter(), () -> new MissingRequiredInputException("issuer.accessParameter"));
//        private final String xForwardedFor;
//        private final String hostAddress;
//        private final String instanceName;

        Optional<Terminal> terminal = terminalService.findTerminalByCode(request.getRecipient().getTerminalCode());
        ValidationUtils.checkEmptyOptional(terminal, () -> new InvalidInputException("terminalCode"));

        AbstractOtpProvider provider = providers.get(request.getOtpType());
        if (Objects.isNull(provider)) {
            return createInvalidResponse(request, "Unsupported otpType.");
        }
//        IssuerInfo issuerInfo = RequestUtils.extractIssuerInfo();
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
//                .otpCode("456")
//                .expireTime(Instant.now())
                .build();
    }

    private OtpVerifyResponse createInvalidVerifyResponse(OtpVerifyRequest request, String errorMessage) {
        Otp otp = Otp.builder()
                .otpType(request.getOtpType())
                .reason(request.getReason())
                .recipient(request.getRecipient())
//                .issuer(request.getIssuer())
                .build();
        return OtpVerifyResponse.builder()
                .isSuccessful(false)
                .errorMessage(errorMessage)
//                .otpCode("456")
//                .expireTime(Instant.now())
                .build();
    }

    public OtpVerifyResponse verifyOtp(OtpVerifyRequest request) {
        AbstractOtpProvider provider = providers.get(request.getOtpType());
        if (Objects.isNull(provider)) {
            return createInvalidVerifyResponse(request, "Unsupported otpType.");
        }
        try {
            return provider.verifyOtp(request);
        } catch (BaseOtpException e) {
            return createInvalidVerifyResponse(request, e.getMessage());
        }
    }

}

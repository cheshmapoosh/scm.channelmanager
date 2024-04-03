package ir.daneshrefah.scm.uaa.service.otp;

import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.model.person.PersonType;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.common.service.terminal.TerminalService;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.domain.otp.OtpType;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpSendRequest;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpSendResponse;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpVerifyRequest;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpVerifyResponse;
import ir.daneshrefah.scm.uaa.service.otp.provder.OtpProvider;
import ir.daneshrefah.scm.uaa.service.user.UserService;
import ir.daneshrefah.scm.uaa.utils.UserValidationWrapper;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
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
    private final UserService userService;
    private final Map<OtpType, OtpProvider> providers;

    public OtpService(List<OtpProvider> providers, TerminalService terminalService, UserService userService) {
        this.terminalService = terminalService;
        this.userService = userService;
        this.providers = providers.stream()
                .collect(Collectors.toMap(OtpProvider::getType, Function.identity()));
    }

    public OtpSendResponse sendOtp(OtpSendRequest request, User loggedInUser) {
        if (null == request.getReason()) {
            throw new MissingRequiredInputException("reason");
        }
        if (StringUtils.isEmpty(request.getRecipientUsername()) && null == loggedInUser) {
            throw new MissingRequiredInputException("recipientUsername");
        }
        String terminalCode = request.getTerminalCode();
        if (StringUtils.isEmpty(terminalCode) && null != loggedInUser) {
            terminalCode = loggedInUser.getTerminalCode();
        }
        if (StringUtils.isEmpty(terminalCode)) {
            throw new MissingRequiredInputException("terminalCode");
        }
        Optional<Terminal> terminal = terminalService.findTerminalByCode(terminalCode);
        if (terminal.isEmpty()) {
            throw new InvalidInputException("terminalCode");
        }
        User recipientUser = null;
        if (StringUtils.isNotEmpty(request.getRecipientUsername())) {
            Optional<User> userOptional = userService.loadUserByUsername(request.getRecipientUsername(), terminalCode);
            if (userOptional.isEmpty()) {
                throw new InvalidInputException("recipientUsername");
            }
            recipientUser = userOptional.get();
        }
        if (null == recipientUser) {
            recipientUser = loggedInUser;
        }

        PersonType personType = recipientUser.getPerson().getPersonType();
        if (!PersonType.REAL.equals(personType) && !PersonType.EMPLOYEE.equals(personType)) {
            throw new InvalidInputException("personType");
        }
        String recipient = StringUtils.isNotEmpty(request.getRecipient()) ? request.getRecipient() : recipientUser.getPerson().getMobile1();
        UserValidationWrapper userValidator = new UserValidationWrapper(recipientUser);
        if (StringUtils.isNotEmpty(recipient) && !userValidator.containsMobile(recipient)) {
            throw new InvalidInputException("recipient");
        }
        if (StringUtils.isEmpty(recipient)) {
            throw new MissingRequiredInputException("recipient");
        }
//        TODO need to authorize with recipient password

        return OtpSendResponse.builder()
                .terminalCode(terminalCode)
                .accessParameter(request.getAccessParameter())
                .recipientUsername(recipientUser.getNickname())
                .recipient(recipient)
                .otpType(request.getOtpType())
                .reason(request.getReason())
                .isSuccessful(true)
                .otpCode("456")
                .expireTime(Instant.now())
                .build();
    }

    public OtpVerifyResponse verifyOtp(OtpVerifyRequest request) {
        System.out.println("verify otp request");
        return OtpVerifyResponse.builder()
                .terminalCode(request.getTerminalCode())
                .accessParameter(request.getAccessParameter())
                .recipientUsername(request.getRecipientUsername())
                .recipient(request.getRecipient())
                .otpType(request.getOtpType())
                .reason(request.getReason())
                .isSuccessful("456".equals(request.getClaimCode()))
                .tryCount(1)
                .build();
    }

}

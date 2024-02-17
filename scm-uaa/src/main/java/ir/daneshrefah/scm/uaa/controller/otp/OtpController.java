package ir.daneshrefah.scm.uaa.controller.otp;

import ir.daneshrefah.scm.common.exception.AuthenticationRequiredException;
import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.model.person.PersonType;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.domain.otp.OtpReason;
import ir.daneshrefah.scm.uaa.domain.otp.OtpType;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpSendResponse;
import ir.daneshrefah.scm.uaa.service.user.UserService;
import ir.daneshrefah.scm.uaa.service.otp.OtpService;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpSendRequest;
import ir.daneshrefah.scm.uaa.utils.AuthenticationUtils;
import ir.daneshrefah.scm.uaa.utils.UserValidationWrapper;
import ir.daneshrefah.scm.utils.string.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-05
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/public/otp")
public class OtpController {

    private final UserService userService;
    private final OtpService otpService;

    @GetMapping("/sms")
    public OtpSendResponse sendOtpSms(@RequestBody SmsOtpSendRequest request, HttpServletRequest httpRequest) {
        User loggedInUser = AuthenticationUtils.getLoggedInUser();
        if (null == loggedInUser) {
            throw new AuthenticationRequiredException();
        }
        PersonType personType = loggedInUser.getPerson().getPersonType();
        if (!PersonType.REAL.equals(personType) && !PersonType.EMPLOYEE.equals(personType)) {
            throw new InvalidInputException("person type");
        }
        if (null == request.getReason()) {
            throw new MissingRequiredInputException("reason");
        }
        Optional<User> user = userService.loadUserByUsername(loggedInUser.getNickname(), loggedInUser.getTerminalCode());
        if (user.isEmpty()) {
            throw new NoMatchRecordFoundException("user");
        }
        String recipient = loggedInUser.getPerson().getMobile1();
        UserValidationWrapper userValidator = new UserValidationWrapper(user.get());
        if (StringUtils.isEmpty(recipient)) {
            recipient = user.get().getPerson().getMobile1();
        }
        if (StringUtils.isNotEmpty(recipient) && !userValidator.containsMobile(recipient)) {
            throw new InvalidInputException("recipient");
        }
        if (StringUtils.isEmpty(recipient)) {
            throw new MissingRequiredInputException("recipient");
        }
        OtpSendRequest otpRequest = OtpSendRequest.builder()
                .issuerAddress(httpRequest.getRemoteHost())
                .issuerUsername(loggedInUser.getPerson().getUsername())
                .terminalCode(loggedInUser.getTerminalCode())
                .accessParameter(StringUtils.join(loggedInUser.getAccessParameters().stream().toList(), ","))
                .recipientUsername(loggedInUser.getPerson().getUsername())
                .recipient(recipient)
                .otpType(OtpType.SMS)
                .reason(request.getReason())
                .build();

        return otpService.sendOtp(otpRequest);
    }

}

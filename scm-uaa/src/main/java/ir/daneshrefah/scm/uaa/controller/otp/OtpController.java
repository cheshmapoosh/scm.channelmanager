package ir.daneshrefah.scm.uaa.controller.otp;

import ir.daneshrefah.scm.common.model.person.PersonType;
import ir.daneshrefah.scm.common.exception.ValidationException;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.domain.otp.OtpType;
import ir.daneshrefah.scm.uaa.service.UserService;
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

import static ir.daneshrefah.scm.uaa.common.utils.ErrorCodes.*;

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
    public void sendOtpSms(@RequestBody(required = false) SmsOtpSendRequest request, HttpServletRequest httpRequest) {
        User loggedInUser = AuthenticationUtils.getLoggedInUser();
        if (null == request) {
            throw new ValidationException("otp", ERROR_CODE_REQUEST_IS_NULL, "request body is empty.");
        }
        if (null == loggedInUser) {
            throw new ValidationException("otp", ERROR_CODE_AUTHENTICATION_REQUIRED, "you are not authenticated.");
        }
        PersonType personType = loggedInUser.getPerson().getPersonType();
        if (!PersonType.REAL.equals(personType) && !PersonType.EMPLOYEE.equals(personType)) {
            throw new ValidationException("otp", ERROR_CODE_PERSON_TYPE_NOT_SUPPORT, "person type not supported.");
        }
        if (null == request.getReason()) {
            throw new ValidationException("otp", ERROR_CODE_REASON_IS_NULL, "reason is null.");
        }
        Optional<User> user = userService.loadUserByUsername(loggedInUser.getNickname(), loggedInUser.getTerminalCode());
        if (user.isEmpty()) {
            throw new ValidationException("otp", ERROR_CODE_USER_ID_IS_INVALID, "user not found.");
        }
        String recipient = loggedInUser.getPerson().getMobile1();
        UserValidationWrapper userValidator = new UserValidationWrapper(user.get());
        if (StringUtils.isEmpty(recipient)) {
            recipient = user.get().getPerson().getMobile1();
        }
        if (StringUtils.isNotEmpty(recipient) && !userValidator.containsMobile(recipient)) {
            throw new ValidationException("otp", ERROR_CODE_RECIPIENT_IS_INVALID, "user recipient info is invalid.");
        }
        if (StringUtils.isEmpty(recipient)) {
            throw new ValidationException("otp", ERROR_CODE_RECIPIENT_IS_NULL, "user recipient info is null.");
        }
        OtpSendRequest otpRequest = OtpSendRequest.builder()
                .issuerAddress(httpRequest.getRemoteHost())
                .issuerUsername(loggedInUser.getPerson().getUsername())
                .recipientUser(loggedInUser)
                .recipient(recipient)
                .otpType(OtpType.SMS)
                .reason(request.getReason())
                .build();

        otpService.sendOtp(otpRequest);
    }

}

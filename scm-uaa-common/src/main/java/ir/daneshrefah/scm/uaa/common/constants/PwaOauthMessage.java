package ir.daneshrefah.scm.uaa.common.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum PwaOauthMessage {

    CLIENT_INVALID_APP_VERSION("application version is not supported", HttpStatus.BAD_REQUEST),
    CLIENT_INACTIVE("your username has not been registered yet", HttpStatus.FORBIDDEN),
    CLIENT_ALREADY_ACTIVATED("your username is already registered", HttpStatus.ALREADY_REPORTED),
    CLIENT_USERNAME_MODIFIED("clients username has been modified successfully", HttpStatus.OK),
    CLIENT_USERNAME_MODIFICATION_FAILED("clients username modification failed", HttpStatus.FORBIDDEN),
    CLIENT_INVALID_OTP("entered OTP code is not valid", HttpStatus.NOT_ACCEPTABLE),
    CLIENT_NOT_FOUND("client not found", HttpStatus.UNAUTHORIZED),
    CLIENT_VALIDATION_FAILED("failed to validate client", HttpStatus.FORBIDDEN),
    CLIENT_SUCCESSFULLY_ACTIVATED("client has been activated successfully", HttpStatus.OK),
    CLIENT_REGISTRATION_SENT("client registration code has been sent successfully", HttpStatus.OK),
    CLIENT_REGISTRATION_FAILED("client registration process failed", HttpStatus.FORBIDDEN),
    CLIENT_SUCCESSFULLY_LOGGED_IN("client has logged in successfully", HttpStatus.OK),
    CLIENT_LOGIN_FAILED("client login process failed", HttpStatus.FORBIDDEN),
    INVALID_CREDENTIALS("no channel authentication available", HttpStatus.UNAUTHORIZED),
    INVALID_ACCESS_PARAM("user access location is invalid", HttpStatus.UNAUTHORIZED),
    REGISTRATION_ALREADY_SENT("registration sms is already sent, try again after 2 minutes!", HttpStatus.NOT_ACCEPTABLE),
    CLIENT_INVALID_REQUEST("client request does not provide appropriate parameters", HttpStatus.BAD_REQUEST),
    REACHED_TRIAL_LIMIT("you've reached maximum failed trials, try getting a new code after 2 minutes!", HttpStatus.NOT_ACCEPTABLE),
    REACHED_LOGIN_LIMIT("you've reached maximum failed trials,try again after 10 minutes!", HttpStatus.NOT_ACCEPTABLE);

    private final String text;
    private final HttpStatus httpStatus;

    public static Optional<PwaOauthMessage> findByText(String text) {
        return Arrays.stream(values())
                .filter(v -> v.name().equalsIgnoreCase(text))
                .findFirst();
    }

}

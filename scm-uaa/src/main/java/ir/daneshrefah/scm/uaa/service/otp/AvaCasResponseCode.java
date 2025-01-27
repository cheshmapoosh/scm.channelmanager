package ir.daneshrefah.scm.uaa.service.otp;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum AvaCasResponseCode {

    OK("ok", "0"),
    WRONG_USERNAME_AND_OR_PASSWORD("wrong-username-and-or-password", "1"),
    USER_BLOCKED("user-blocked", "2"),
    USER_UNBLOCKED("user-unblocked", "3"),
    NATIONAL_CODE_EXISTS("national-code-exists", "4"),
    USERNAME_NOT_FOUND("username-not-found", "5"),
    INTERNAL_ERROR("internal-error", "6"),
    USER_ALREADY_DISABLED("user-already-disabled", "7");

    private final String status;
    private final String code;

    public static String getStatus(String code) {
        return Arrays.stream(AvaCasResponseCode.values())
                .filter(avaCasResponseCode -> avaCasResponseCode.getCode().equals(code))
                .map(AvaCasResponseCode::getStatus)
                .findFirst()
                .orElse(null);
    }
}

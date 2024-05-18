package ir.daneshrefah.scm.uaa.common.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum AuthorizationGrantType {

    AUTHORIZATION_CODE("authorization_code", true, true),
    REFRESH_TOKEN("refresh_token", false, false),
    CLIENT_CREDENTIALS("client_credentials", true, false),
    FIRST_PASSWORD("first_password", true, true),
    SECOND_PASSWORD("second_password", false, false),
    SHAHKAR("ext_shk", false, false);

    private final String code;
    private final boolean supportClientCheck;
    private final boolean supportActivationCheck;

    public static AuthorizationGrantType findByCode(String code) {
        return Arrays.stream(AuthorizationGrantType.values())
                .filter(s -> s.code.equals(code))
                .findFirst()
                .orElse(null);
    }

}

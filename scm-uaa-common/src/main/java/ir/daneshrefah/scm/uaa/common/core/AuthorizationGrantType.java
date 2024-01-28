package ir.daneshrefah.scm.uaa.common.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthorizationGrantType {

    AUTHORIZATION_CODE("authorization_code", true),
    REFRESH_TOKEN("refresh_token", false),
    CLIENT_CREDENTIALS("client_credentials", true),
    FIRST_PASSWORD("first_password", true),
    SECOND_PASSWORD("second_password", false);

    private final String code;
    private final boolean supportClientCheck;

}

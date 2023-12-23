package ir.daneshrefah.scm.uaa.common.core;

public enum AuthorizationGrantType {

    AUTHORIZATION_CODE("authorization_code"),
    REFRESH_TOKEN("refresh_token"),
    CLIENT_CREDENTIALS("client_credentials"),
    FIRST_PASSWORD("first_password"),
    SECOND_PASSWORD("second_password");

    private String code;

    AuthorizationGrantType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}

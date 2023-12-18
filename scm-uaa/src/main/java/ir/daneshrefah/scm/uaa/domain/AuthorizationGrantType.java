package ir.daneshrefah.scm.uaa.domain;

public enum AuthorizationGrantType {

    AUTHORIZATION_CODE(org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE.getValue()),
    REFRESH_TOKEN(org.springframework.security.oauth2.core.AuthorizationGrantType.REFRESH_TOKEN.getValue()),
    CLIENT_CREDENTIALS(org.springframework.security.oauth2.core.AuthorizationGrantType.CLIENT_CREDENTIALS.getValue()),
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

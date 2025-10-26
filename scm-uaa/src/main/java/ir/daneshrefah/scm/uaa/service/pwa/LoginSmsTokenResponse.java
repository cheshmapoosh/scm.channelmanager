package ir.daneshrefah.scm.uaa.service.pwa;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LoginSmsTokenResponse {

    /*
            // SAMPLE
            {
              "access_token": null,
              "token_type": "bearer",
              "scope": "openid",
              "iat": 1759220761,
              "firstName": "علی",
              "lastName": "تست",
              "x_is_otp_enabled": "OTP_ENABLED",
              "secondAuthenticationMethod": "SMS",
              "appVersion": "PWA",
              "grn": "password",
              "aut": "ROLE_CUSTOMER",
              "warnUserToChangePassword": "false",
              "lastChangePassword": "1404/06/29 00:00",
              "jti": "5c15d0d7-4cf3-47f3-9f7f-d0d07d699536",
              "hasRegistryTokenFromCookie": true
            }
     */
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("token_type")
    private String tokenType;
    private String scope;
    private long iat;
    private String firstName;
    private String lastName;
    @JsonProperty("x_is_otp_enabled")
    private String otpEnabled;
    private String secondAuthenticationMethod;
    private String appVersion;
    private String grn;
    private String aut;
    private String warnUserToChangePassword;
    private String lastChangePassword;
    private String jti;
    @JsonProperty("hasRegistryTokenFromCookie")
    private boolean hasRegistryTokenFromCookie;

}

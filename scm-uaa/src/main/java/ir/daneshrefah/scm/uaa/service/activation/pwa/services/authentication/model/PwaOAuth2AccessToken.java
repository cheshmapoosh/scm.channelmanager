package ir.daneshrefah.scm.uaa.service.activation.pwa.services.authentication.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import ir.daneshrefah.scm.common.model.user.AuthenticationMethod;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class PwaOAuth2AccessToken implements Serializable {

    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("token_type")
    private String tokenType;
    @JsonProperty("refresh_token")
    private String refreshToken;
    @JsonProperty("expires_in")
    private Integer expiresIn;
    private String scope;
    private Long iat;
    private String firstName;
    private String lastName;
    @JsonProperty("x_is_otp_enabled")
    private String otpStatus;
    private AuthenticationMethod secondAuthenticationMethod;
    private String appVersion;
    private String grn;
    private String aut;
    private String warnUserToChangePassword;
    private String lastChangePassword;
    private String jti;
    private Boolean hasRegistryTokenFromCookie;
    @JsonIgnore
    private Date expirationDate;
    @JsonIgnore
    private String realUsername;

}

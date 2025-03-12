package ir.daneshrefah.scm.uaa.service.client.dto;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
import ir.daneshrefah.scm.common.model.person.PersonType;
import ir.daneshrefah.scm.common.validation.NotBlankIfPresent;
import ir.daneshrefah.scm.common.validation.Numeric;
import ir.daneshrefah.scm.common.validation.Password;
import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.domain.client.ClientAuthenticationMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class ClientCreateRequest implements RequestData {
    /**
     * REAL , UNKNOWN , EMPLOYEE NO ACCEPTABLE.
     */
    @NotNull
    private PersonType personType;
    @NotNull
    @NotBlank
    private String nationalId;
    private String subOrg;
    @NotNull
    @NotBlank
    @Size(min = 1,max = 10)
    private String nickname;
    @NotNull
    @NotBlank
    private String title;
    @NotNull
    @NotBlank
    private String titleFa;
    @Password
    private String password;
    @NotNull
    @NotBlank
    private String terminalCode;
    @NotNull
    private Boolean requireAuthorizationConsent;
    @NotNull
    private Boolean requireProofKey;
    @NotNull
    private Boolean checkVersion;
    @NotNull
    private Boolean checkActivation;
    @Numeric
    @NotNull
    private String sessionTimeToLiveMinute;
    @NotNull
    private Boolean checkIpAddress;
    @NotNull
    private List<ClientAuthenticationMethod> authenticationMethods;
    @NotNull
    private List<AuthorizationGrantType> authorizationGrantTypes;
    @NotNull
    private List<String> redirectUris;
    @NotNull
    private Set<String> allowIpAddresses;

}

package ir.daneshrefah.scm.uaa.service.client.dto;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.domain.client.ClientAuthenticationMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class ClientCreateRequest implements RequestData {
    @NotNull
    @NotBlank
    private String clientId;
    @NotNull
    @NotBlank
    private String title;
    private String clientSecret;
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
    private Long sessionTimeToLiveMinute;
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

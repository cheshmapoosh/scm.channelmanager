package ir.daneshrefah.scm.uaa.domain.client;

import ir.daneshrefah.scm.uaa.domain.AuthorizationGrantType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Client {

    private String id;
    private String clientId;
    private String clientSecret;
    private ClientAuthenticationMethod authenticationMethod;
    private List<AuthorizationGrantType> authorizationGrantTypes;
    private List<String> redirectUrls;
    private String terminalCode;
    private List<String> scopes;
    private boolean requireAuthorizationConsent;
    private boolean requireProofKey;


}

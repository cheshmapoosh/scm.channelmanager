package ir.daneshrefah.scm.uaa.domain.client;

import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-17
 */
@Getter
@Setter
public class Client {

    private String id;
    private String title;
    private String clientId;
    private String clientSecret;
    private List<ClientAuthenticationMethod> authenticationMethods;
    private List<AuthorizationGrantType> authorizationGrantTypes;
    private List<String> redirectUrls;
    private String terminalCode;
    private List<String> scopes;
    private boolean requireAuthorizationConsent;
    private boolean requireClientAuthentication;
    private boolean requireProofKey;
    private boolean checkVersion;
    private boolean checkActivation;
    private List<ClientVersion> versions;
    private Long sessionTimeToLive;

}

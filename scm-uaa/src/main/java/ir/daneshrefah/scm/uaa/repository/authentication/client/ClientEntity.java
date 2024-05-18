package ir.daneshrefah.scm.uaa.repository.authentication.client;

import ir.daneshrefah.scm.common.data.converter.StringSetConverter;
import ir.daneshrefah.scm.common.data.entity.AbstractDefaultEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-04
 */
@Getter
@Setter
@Entity
@Table(name = "TBL_SUA_CLIENT")
public class ClientEntity extends AbstractDefaultEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CLIENT_ID")
    private Long id;
    private String title;
    @Column(name = "CLIENT_IDENTIFIER")
    private String clientId;
    @Column(name = "CLIENT_SECRET")
    private String clientSecret;
    private String terminalCode;
    @Column(name = "CLIENT_AUTH_METHOD_BASIC")
    private boolean clientAuthenticationMethodSecretBasic;
    @Column(name = "CLIENT_AUTH_METHOD_POST")
    private boolean clientAuthenticationMethodSecretPost;
    @Column(name = "CLIENT_AUTH_METHOD_SEC_JWT")
    private boolean clientAuthenticationMethodSecretJwt;
    @Column(name = "CLIENT_AUTH_METHOD_KEY_JWT")
    private boolean clientAuthenticationMethodKeyJwt;
//    @Column(name = "AUTH_GRANT_AUTHORIZATION_CODE")
//    private boolean authorizationGrantTypeAuthorizationCode;
//    @Column(name = "AUTH_GRANT_REFRESH_TOKEN")
//    private boolean authorizationGrantTypeRefreshToken;
//    @Column(name = "AUTH_GRANT_CLIENT_CREDENTIAL")
//    private boolean authorizationGrantTypeClientCredential;
//    @Column(name = "AUTH_GRANT_FIRST_PASSWORD")
//    private boolean authorizationGrantTypeFirstPassword;
//    @Column(name = "AUTH_GRANT_SECOND_PASSWORD")
//    private boolean authorizationGrantTypeSecondPassword;
    @Column(name = "REDIRECT_URIS")
    @Convert(converter = StringSetConverter.class)
    private Set<String> redirectUris;
    private boolean requireAuthorizationConsent;
    private boolean requireClientAuthentication;
    private boolean requireProofKey;
    private boolean checkVersion;
    private boolean checkActivation;
    private Long sessionTimeToLiveMinute;
//    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
//    private Set<ClientScopeRelation> scopes;
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<ClientVersionEntity> versions;
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ClientAuthorizationGrantTypeEntity> authorizationGrantTypes;
}

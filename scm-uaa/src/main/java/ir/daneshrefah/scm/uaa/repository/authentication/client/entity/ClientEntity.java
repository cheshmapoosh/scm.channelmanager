package ir.daneshrefah.scm.uaa.repository.authentication.client.entity;

import ir.daneshrefah.scm.common.data.converter.StringSetConverter;
import ir.daneshrefah.scm.common.data.entity.AbstractEntity;
import ir.daneshrefah.scm.common.data.entity.person.GeneralLegalPersonEntity;
import ir.daneshrefah.scm.uaa.repository.authentication.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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
public class ClientEntity extends AbstractEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CLIENT_ID")
    private Long id;
    private String terminalCode;
    @Column(name = "CLIENT_AUTH_METHOD_BASIC")
    private boolean clientAuthenticationMethodSecretBasic;
    @Column(name = "CLIENT_AUTH_METHOD_POST")
    private boolean clientAuthenticationMethodSecretPost;
    @Column(name = "CLIENT_AUTH_METHOD_SEC_JWT")
    private boolean clientAuthenticationMethodSecretJwt;
    @Column(name = "CLIENT_AUTH_METHOD_KEY_JWT")
    private boolean clientAuthenticationMethodKeyJwt;
    @Column(name = "CLIENT_AUTH_METHOD_NONE")
    private boolean clientAuthenticationMethodNone;
    @Column(name = "REDIRECT_URIS")
    @Convert(converter = StringSetConverter.class)
    private Set<String> redirectUris;
    @Column(name = "REQUIRE_AUTH_CONSENT")
    private boolean requireAuthorizationConsent;
    private boolean requireProofKey;
    private boolean checkVersion;
    private boolean checkActivation;
    private boolean checkIpAddress;
    private String allowIpAddresses;
    @Column(name = "SESSION_TTL_MINUTE")
    private Long sessionTimeToLiveMinute;
    //    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
//    private Set<ClientScopeRelation> scopes;
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<ClientVersionEntity> versions;
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<ClientAuthorizationGrantTypeEntity> authorizationGrantTypes;
    /**
     * The client correspond user,the target person type of this user is CLIENT.
     */
    @OneToOne
    @JoinColumn(name = "USER_CHANNEL_AUTHENTICATION_ID")
    private UserEntity user;
    /**
     * Main legal user that can contains (CORPORAT,EGOVERNANCE,BANK,TAMIN) person types.
     */
    @OneToOne
    @JoinColumn(name = "LEGAL_USER_ID")
    private GeneralLegalPersonEntity legalPerson;
    private Boolean status;
}

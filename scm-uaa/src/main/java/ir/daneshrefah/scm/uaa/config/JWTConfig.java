package ir.daneshrefah.scm.uaa.config;

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import ir.daneshrefah.scm.common.model.person.GeneralLegalPerson;
import ir.daneshrefah.scm.common.model.person.GeneralRealPerson;
import ir.daneshrefah.scm.common.model.person.PersonType;
import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.domain.client.Client;
import ir.daneshrefah.scm.uaa.security.token.AbstractAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.PostAuthenticationToken;
import ir.daneshrefah.scm.uaa.service.client.ClientService;
import ir.daneshrefah.scm.utils.date.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.token.*;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static ir.daneshrefah.scm.common.constant.SecurityConstants.ROLE_PERSON_TYPE_CLIENT;
import static ir.daneshrefah.scm.uaa.common.utils.Constants.*;


@Configuration
public class JWTConfig {
    @Value("${scm.security.key-store.name}")
    private String keyStoreFilePath;
    @Value("${scm.security.key-store.password}")
    private String keyStorePassword;
    @Value("${scm.security.key-store.alias}")
    private String keyStoreAlias;


    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration
                .jwtDecoder(jwkSource);
    }

    @Bean
    @Primary
    OAuth2TokenGenerator<?> uaaTokenGenerator(JWKSource<SecurityContext> jwkSource, ClientService clientService) {
        NimbusJwtEncoder jwtEncoder = new NimbusJwtEncoder(jwkSource);
        JwtGenerator jwtGenerator = new JwtGenerator(jwtEncoder);
        jwtGenerator.setJwtCustomizer(jwtCustomizer(clientService));
        OAuth2RefreshTokenGenerator refreshTokenGenerator = new OAuth2RefreshTokenGenerator();
        return new DelegatingOAuth2TokenGenerator(jwtGenerator, refreshTokenGenerator);
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer(ClientService clientService) {
        return context -> {
            JwtClaimsSet.Builder claims = context.getClaims();
            if (PostAuthenticationToken.class.isAssignableFrom(context.getPrincipal().getClass()) &&
                    PostAuthenticationToken.AuthenticationStatus.INCOMPLETE.equals(((PostAuthenticationToken) context.getPrincipal()).getAuthenticationStatus())) {
                PostAuthenticationToken principal = context.getPrincipal();
                User user = principal.getPrincipal().getUser();
                String terminalCode = user.getTerminalCode();
                claims.claim(CLAIM_KEY_TERMINAL, terminalCode);
                claims.claim(CLAIM_KEY_LOGIN_AUTH_METHOD, user.getLoginAuthenticationMethod().getCode());
                addTokenLifeTimeClaims(principal,claims);
            } else if (PostAuthenticationToken.class.isAssignableFrom(context.getPrincipal().getClass()) &&
                    PostAuthenticationToken.AuthenticationStatus.AUTHENTICATED.equals(((PostAuthenticationToken) context.getPrincipal()).getAuthenticationStatus())) {
                PostAuthenticationToken principal = context.getPrincipal();
                User user = principal.getPrincipal().getUser();
                String terminalCode = user.getTerminalCode();
                claims.claim(CLAIM_KEY_TERMINAL, terminalCode);
                claims.claim(CLAIM_KEY_GRANT, principal.getDetails().getGrantType());
                claims.claim(CLAIM_KEY_LOGIN_AUTH_METHOD, user.getLoginAuthenticationMethod().getCode());
                claims.claim(CLAIM_KEY_TRANSACTION_AUTH_METHOD,
                        Optional.ofNullable(user.getTransactionAuthenticationMethod().getCode())
                                .orElse(ir.daneshrefah.scm.utils.string.StringUtils.EMPTY));
                claims.claim(CLAIM_KEY_AUTHORITIES, principal.getDetails().getAuthorities().toString());
                String sessionKey = principal.getSessionId();
                if (StringUtils.isNotEmpty(sessionKey)) {
                    claims.claim(CLAIM_KEY_SESSION, sessionKey);
                }
                if (null != user.getAccessParameters() && !user.getAccessParameters().isEmpty())
                    claims.claim(CLAIM_KEY_ACCESS_PARAMETER, user.getAccessParameters());
                PersonType personType = user.getPerson().getPersonType();
                claims.claim(CLAIM_KEY_PERSON_NATIONALITY, user.getPerson().getNationality().getCode());
                claims.claim(CLAIM_KEY_PERSON_TYPE, user.getPerson().getPersonType().getCode());
                claims.claim(CLAIM_KEY_PERSON_IDENTIFIER, user.getPerson().getId());
                claims.claim(CLAIM_KEY_PERSON_PROFILE_IDENTIFIER, user.getPerson().getUsername());
                switch (personType) {
                    case REAL, EMPLOYEE:
                        claims.claim(CLAIM_KEY_PERSON_NATIONAL_ID, ((GeneralRealPerson) user.getPerson()).getNationalCode());
                        claims.claim(CLAIM_KEY_PERSON_FIRST_NAME, ((GeneralRealPerson) user.getPerson()).getFirstName());
                        claims.claim(CLAIM_KEY_PERSON_LAST_NAME, ((GeneralRealPerson) user.getPerson()).getLastName());
                        break;
                    case CORPORATE, GOVERNANCE, BANK, TAMIN:
                        claims.claim(CLAIM_KEY_PERSON_NATIONAL_ID, ((GeneralLegalPerson) user.getPerson()).getNationalId());
                        if (StringUtils.isNotEmpty(((GeneralLegalPerson) user.getPerson()).getSubOrganizationId())) {
                            claims.claim(CLAIM_KEY_PERSON_SUB_ORGANIZATION_ID, ((GeneralLegalPerson) user.getPerson()).getSubOrganizationId());
                        }
                        claims.claim(CLAIM_KEY_PERSON_TITLE, ((GeneralLegalPerson) user.getPerson()).getTitle());
                        break;
                }
                addTokenLifeTimeClaims(principal,claims);
            } else if (OAuth2ClientAuthenticationToken.class.isAssignableFrom(context.getPrincipal().getClass())) {
                OAuth2ClientAuthenticationToken principal = context.getPrincipal();
                long id = Long.valueOf(principal.getRegisteredClient().getId());
                Optional<Client> client = clientService.findById(id);
                claims.claim(CLAIM_KEY_TERMINAL, principal.getRegisteredClient().getClientSettings().getSetting(CLIENT_SETTING_KEY_TERMINAL_CODE));
                claims.claim(CLAIM_KEY_GRANT, AuthorizationGrantType.CLIENT_CREDENTIALS);
                claims.claim(CLAIM_KEY_PERSON_TYPE, PersonType.CLIENT.getCode());
                claims.claim(CLAIM_KEY_PERSON_IDENTIFIER, id);
                claims.claim(CLAIM_KEY_PERSON_PROFILE_IDENTIFIER, principal.getRegisteredClient().getClientId());
                claims.claim(CLAIM_KEY_PERSON_TITLE, client.get().getTitle());
                List<String> authorities = clientService.loadClientAuthorities(id).orElse(new ArrayList<>());
                authorities.add(ROLE_PERSON_TYPE_CLIENT);
                claims.claim(CLAIM_KEY_AUTHORITIES, authorities.toString());
                addTokenLifeTimeClaims(principal,claims);
            } else if (AbstractAuthenticationToken.class.isAssignableFrom(context.getPrincipal().getClass()) &&
                    context.getPrincipal().isAuthenticated()) {
                AbstractAuthenticationToken authenticationToken = context.getPrincipal();
                RegisteredClient client = context.getRegisteredClient();
                claims.claim(CLAIM_KEY_TERMINAL, client.getClientSettings().getSetting(CLIENT_SETTING_KEY_TERMINAL_CODE));
                claims.claim(CLAIM_KEY_GRANT, authenticationToken.getGrantType());
                claims.claim(CLAIM_KEY_AUTHORITIES, authenticationToken.getAuthorities().toString());
//                String sessionKey = authenticationToken.getSessionId();
//                if (StringUtils.isNotEmpty(sessionKey)) {
//                    claims.claim(CLAIM_KEY_SESSION, sessionKey);
//                }
                if (StringUtils.isNotEmpty(authenticationToken.getAccessParameter())) {
                    claims.claim(CLAIM_KEY_ACCESS_PARAMETER, authenticationToken.getAccessParameter());
                }
                claims.claim(CLAIM_KEY_PERSON_TYPE, PersonType.UNKNOWN.getCode());
                AbstractAuthenticationToken principal = context.getPrincipal();
                addTokenLifeTimeClaims(principal,claims);
            }
        };
    }


    @Bean
    public JWKSource<SecurityContext> jwkSource(JWKSet jwkSet) throws GeneralSecurityException, IOException {
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

    @Bean
    public JWKSet jwkSet() throws GeneralSecurityException, IOException {
        RSAKey rsaKey = generateRsa();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return jwkSet;
    }


    private RSAKey generateRsa() throws GeneralSecurityException, IOException {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        return new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .algorithm(Algorithm.parse("RS256"))
                .build();
    }

    @Bean
    public KeyPair generateRsaKey() throws GeneralSecurityException, IOException {

        InputStream p12FileInputStream = JwtDecoder.class.getClassLoader().getResourceAsStream(keyStoreFilePath);

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(p12FileInputStream, keyStorePassword.toCharArray());

        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(keyStoreAlias, new KeyStore.PasswordProtection(keyStorePassword.toCharArray()));
        PrivateKey privateKey = privateKeyEntry.getPrivateKey();
        X509Certificate certificate = (X509Certificate) privateKeyEntry.getCertificate();

        KeyPair keyPair = new KeyPair(certificate.getPublicKey(), privateKey);

        return keyPair;
    }

    private void addTokenLifeTimeClaims(PostAuthenticationToken principal, JwtClaimsSet.Builder claims) {
        long timeToLiveMinutes = DateUtils.InstantTools.calculateMinutesBetween(principal.getIssuedAt(), principal.getExpiresAt());
        claims.claim(CLAIM_KEY_TIME_TO_LIVE,timeToLiveMinutes);
        claims.claim(CLAIM_KEY_MAX_IDLE_TIME,timeToLiveMinutes);
    }

    private void addTokenLifeTimeClaims(OAuth2ClientAuthenticationToken principal, JwtClaimsSet.Builder claims) {
        RegisteredClient registeredClient = principal.getRegisteredClient();
        addTokenByRegisteredClient(registeredClient,claims);
    }

    private void addTokenLifeTimeClaims(AbstractAuthenticationToken principal, JwtClaimsSet.Builder claims) {
        RegisteredClient registeredClient = principal.getRegisteredClient();
        addTokenByRegisteredClient(registeredClient,claims);
    }


    private void addTokenByRegisteredClient(RegisteredClient registeredClient, JwtClaimsSet.Builder claims) {
        if (Objects.nonNull(registeredClient) && Objects.nonNull(registeredClient.getClientIdIssuedAt())) {
            long timeToLiveMinutes = DateUtils.InstantTools.calculateMinutesBetween(registeredClient.getClientIdIssuedAt(), registeredClient.getClientSecretExpiresAt());
            claims.claim(CLAIM_KEY_TIME_TO_LIVE,timeToLiveMinutes);
            claims.claim(CLAIM_KEY_MAX_IDLE_TIME,timeToLiveMinutes);
        }
    }

//    @Bean
//    public BCryptPasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }


}

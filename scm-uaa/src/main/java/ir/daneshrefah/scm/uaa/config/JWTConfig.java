package ir.daneshrefah.scm.uaa.config;

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.common.model.person.CorporatePerson;
import ir.daneshrefah.scm.uaa.common.model.person.EmployeePerson;
import ir.daneshrefah.scm.uaa.common.model.person.IndividualPerson;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.common.type.PersonType;
import ir.daneshrefah.scm.uaa.security.token.PostAuthenticationToken;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
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
import java.util.Optional;

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
    OAuth2TokenGenerator<?> uaaTokenGenerator(JWKSource<SecurityContext> jwkSource) {
        NimbusJwtEncoder jwtEncoder = new NimbusJwtEncoder(jwkSource);
        JwtGenerator jwtGenerator = new JwtGenerator(jwtEncoder);
        jwtGenerator.setJwtCustomizer(jwtCustomizer());
        return new DelegatingOAuth2TokenGenerator(jwtGenerator);
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer() {
        return context -> {
            JwtClaimsSet.Builder claims = context.getClaims();
            if (PostAuthenticationToken.class.isAssignableFrom(context.getPrincipal().getClass())) {
                PostAuthenticationToken principal = context.getPrincipal();
                User user = principal.getDetails().getUser();
                String terminalCode = user.getTerminalCode();
                claims.claim(CLAIM_KEY_TERMINAL, terminalCode);
                claims.claim(CLAIM_KEY_GRANT, principal.getPreAuthenticationToken().getGrantType());
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
                PersonType personType = user.getPerson().getType();
                claims.claim(CLAIM_KEY_PERSON_NATIONALITY, user.getPerson().getNationality().getCode());
                claims.claim(CLAIM_KEY_PERSON_TYPE, user.getPerson().getType().getCode());
                claims.claim(CLAIM_KEY_PERSON_IDENTIFIER, user.getPerson().getUsername());
                switch (personType) {
                    case INDIVIDUAL_CUSTOMER:
                        claims.claim(CLAIM_KEY_PERSON_NATIONAL_ID, ((IndividualPerson) user.getPerson()).getNationalCode());
                        claims.claim(CLAIM_KEY_PERSON_FIRST_NAME, ((IndividualPerson) user.getPerson()).getFirstName());
                        claims.claim(CLAIM_KEY_PERSON_LAST_NAME, ((IndividualPerson) user.getPerson()).getLastName());
                        break;
                    case EMPLOYEE:
                        claims.claim(CLAIM_KEY_PERSON_NATIONAL_ID, ((EmployeePerson) user.getPerson()).getNationalCode());
                        claims.claim(CLAIM_KEY_PERSON_FIRST_NAME, ((EmployeePerson) user.getPerson()).getFirstName());
                        claims.claim(CLAIM_KEY_PERSON_LAST_NAME, ((EmployeePerson) user.getPerson()).getLastName());
                        break;
                    case CORPORATE_CUSTOMER:
                        claims.claim(CLAIM_KEY_PERSON_NATIONAL_ID, ((CorporatePerson) user.getPerson()).getNationalId());
                        if (StringUtils.isNotEmpty(((CorporatePerson) user.getPerson()).getSubOrganizationId())) {
                            claims.claim(CLAIM_KEY_PERSON_SUB_ORGANIZATION_ID, ((CorporatePerson) user.getPerson()).getSubOrganizationId());
                        }
                        claims.claim(CLAIM_KEY_PERSON_TITLE, ((CorporatePerson) user.getPerson()).getTitle());
                        break;
                }
            } else if (OAuth2ClientAuthenticationToken.class.isAssignableFrom(context.getPrincipal().getClass())) {
                OAuth2ClientAuthenticationToken principal = context.getPrincipal();
                claims.claim(CLAIM_KEY_TERMINAL, principal.getRegisteredClient().getClientSettings().getSetting(CLIENT_SETTING_KEY_TERMINAL_CODE));
                claims.claim(CLAIM_KEY_GRANT, AuthorizationGrantType.CLIENT_CREDENTIALS);
                claims.claim(CLAIM_KEY_PERSON_TYPE, PersonType.CLIENT.getCode());
                claims.claim(CLAIM_KEY_PERSON_IDENTIFIER, principal.getRegisteredClient().getClientId());
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

//    @Bean
//    public BCryptPasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }


}

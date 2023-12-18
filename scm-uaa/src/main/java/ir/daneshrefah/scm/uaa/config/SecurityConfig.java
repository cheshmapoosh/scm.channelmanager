package ir.daneshrefah.scm.uaa.config;

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import ir.daneshrefah.scm.uaa.security.authenticationDetails.TerminalAuthenticationDetailsSource;
import ir.daneshrefah.scm.uaa.security.TerminalLoginUrlAuthenticationEntryPoint;
import ir.daneshrefah.scm.uaa.security.authenticationProvider.GeneralAuthenticationProvider;
import ir.daneshrefah.scm.uaa.security.authenticationProvider.OAuth2GeneralAuthenticationProvider;
import ir.daneshrefah.scm.uaa.security.converter.FirstPasswordGrantAuthenticationConverter;
import ir.daneshrefah.scm.uaa.security.converter.SecondPasswordGrantAuthenticationConverter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-13
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${uaa.key-store.name}")
    private String keyStoreFilePath;
    @Value("${uaa.key-store.password}")
    private String keyStorePassword;
    @Value("${uaa.key-store.alias}")
    private String keyStoreAlias;

    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http)
            throws Exception {

        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                http.getConfigurer(OAuth2AuthorizationServerConfigurer.class);


        authorizationServerConfigurer
                .authorizationEndpoint(authorizationEndpoint ->
                        authorizationEndpoint.consentPage("/consent"))
                .tokenEndpoint(tokenEndpoint ->
                        tokenEndpoint
                                .accessTokenRequestConverters(
                                        converters -> converters.addAll(
                                            Arrays.asList(new FirstPasswordGrantAuthenticationConverter(),
                                                    new SecondPasswordGrantAuthenticationConverter()))
                                )
                                .authenticationProvider(
                                                    new OAuth2GeneralAuthenticationProvider(
                                                            http.getSharedObject(OAuth2AuthorizationService.class),
                                                            http.getSharedObject(OAuth2TokenGenerator.class))
                                )
                )
                .oidc(Customizer.withDefaults());	// Enable OpenID Connect 1.0

        http
                // Redirect to the login page when not authenticated from the
                // authorization endpoint
                .exceptionHandling((exceptions) -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new TerminalLoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )
                )
                // Accept access tokens for User Info and/or Client Registration
                .oauth2ResourceServer((resourceServer) -> resourceServer
                        .jwt(Customizer.withDefaults()));

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http)
            throws Exception {
        http
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/login**").permitAll()
                        .anyRequest().authenticated()
                )
                // Form login handles the redirect to the login page from the
                // authorization server filter chain
//                .formLogin(Customizer.withDefaults());
                .exceptionHandling((exceptions) -> exceptions
                        .accessDeniedHandler(new AccessDeniedHandler() {
                            @Override
                            public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
                                System.out.printf("");
                            }
                        })
                        .defaultAuthenticationEntryPointFor(
                                new TerminalLoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )
                )
                .csrf(csrf -> csrf.ignoringRequestMatchers("/**"))
                .formLogin(login -> {
//                    login.setFormLoginEnabled(true);
                    login.usernameParameter("username");
                    login.passwordParameter("password");
                    login.loginPage("/login");
                    login.failureUrl("/login?error");
                    login.authenticationDetailsSource(new TerminalAuthenticationDetailsSource());
//                    login.setAuthenticationUrl(getLoginProcessingUrl());
                    });
        return http.build();
    }

    @Bean
    public AuthenticationManager authManager(HttpSecurity http, GeneralAuthenticationProvider authProvider) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.authenticationProvider(authProvider);
        return authenticationManagerBuilder.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails userDetails1 = User.withDefaultPasswordEncoder()
                .username("user")
                .password("password")
                .roles("USER")
                .build();
        UserDetails userDetails2 = User.withDefaultPasswordEncoder()
                .username("ib")
                .password("password")
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(userDetails1, userDetails2);
    }

//    @Bean
//    public RegisteredClientRepository registeredClientRepository() {
//        RegisteredClient oidcClient = RegisteredClient.withId(UUID.randomUUID().toString())
//                .clientId("client1")
//                .clientSecret("{noop}myClientSecretValue")
//                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
//                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
//                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
//                .redirectUri("http://127.0.0.1:8080/login/oauth2/code/users-client-oidc")
//                .redirectUri("http://127.0.0.1:8080/authorized")
//                .scope(OidcScopes.OPENID)
//                .scope(OidcScopes.PROFILE)
//                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
//                .build();
//
//        return new InMemoryRegisteredClientRepository(oidcClient);
//    }

    @Bean
    public JWKSource<SecurityContext> jwkSource(JWKSet jwkSet) {
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

    @Bean
    public JWKSet jwkSet() throws GeneralSecurityException, IOException {
        RSAKey rsaKey = generateRsa();
        JWKSet jwkSet=new JWKSet(rsaKey);
        return jwkSet;
    }

    private RSAKey generateRsa() throws GeneralSecurityException, IOException {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        return new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
//                .keyID(UUID.randomUUID().toString())
                .algorithm(Algorithm.parse("RS256"))
                .build();
    }

    private KeyPair generateRsaKey() throws GeneralSecurityException, IOException {
        InputStream p12FileInputStream = JwtDecoder.class.getClassLoader().getResourceAsStream(keyStoreFilePath);

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(p12FileInputStream, keyStorePassword.toCharArray());

        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(keyStoreAlias,
                new KeyStore.PasswordProtection(keyStorePassword.toCharArray()));
        PrivateKey privateKey = privateKeyEntry.getPrivateKey();
        X509Certificate certificate = (X509Certificate) privateKeyEntry.getCertificate();

        KeyPair keyPair = new KeyPair(certificate.getPublicKey(), privateKey);

        return keyPair;
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer("http://auth-server:8000")
                .build();
    }

}

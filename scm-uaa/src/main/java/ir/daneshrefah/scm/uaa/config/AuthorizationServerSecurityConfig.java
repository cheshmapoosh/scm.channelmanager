package ir.daneshrefah.scm.uaa.config;

import ir.daneshrefah.scm.uaa.security.filter.MissingGrantTypeFallbackFilter;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.LegacyAuthProperties;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.converter.LegacyPasswordGrantAuthenticationConverter;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.provider.LegacyPasswordGrantAuthenticationProvider;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.shahkar.ShahkarGrantAuthenticationConverter;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.shahkar.ShahkarGrantAuthenticationProvider;
import ir.daneshrefah.scm.uaa.utils.Urls;
import ir.daneshrefah.scm.uaa.web.login.ClientLoginThemeProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.List;

@Configuration
@EnableConfigurationProperties({
        PwaAuthenticationConfigProperties.class,
        LegacyAuthProperties.class,
        ClientLoginThemeProperties.class
})
@SuppressWarnings("removal")
public class AuthorizationServerSecurityConfig {
    private static final String LOGIN_PROCESS_URI = "/login";

    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(
            HttpSecurity http,
            LegacyPasswordGrantAuthenticationConverter legacyPasswordGrantAuthenticationConverter,
            LegacyPasswordGrantAuthenticationProvider legacyPasswordGrantAuthenticationProvider,
            ShahkarGrantAuthenticationConverter shahkarGrantAuthenticationConverter,
            ShahkarGrantAuthenticationProvider shahkarGrantAuthenticationProvider
    ) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer();
        RequestMatcher endpointsMatcher = authorizationServerConfigurer.getEndpointsMatcher();
        authorizationServerConfigurer
                .authorizationEndpoint(authorizationEndpoint -> authorizationEndpoint.consentPage("/consent"))
                .tokenEndpoint(tokenEndpoint -> tokenEndpoint
                        .accessTokenRequestConverters(converters -> converters.addAll(List.of(
                                legacyPasswordGrantAuthenticationConverter,
                                shahkarGrantAuthenticationConverter
                        )))
                        .authenticationProvider(legacyPasswordGrantAuthenticationProvider)
                        .authenticationProvider(shahkarGrantAuthenticationProvider)
                )
                .oidc(Customizer.withDefaults());

        http
                .addFilterBefore(new MissingGrantTypeFallbackFilter(), BasicAuthenticationFilter.class)
                .securityMatcher(endpointsMatcher)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(Urls.OAUTH2_TOKEN).permitAll()
                        .requestMatchers("/otp/public/**").permitAll()
                        .requestMatchers("/api/access-token/get-first-password-token").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions.defaultAuthenticationEntryPointFor(
                        new LoginUrlAuthenticationEntryPoint(LOGIN_PROCESS_URI),
                        new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                ))
                .oauth2ResourceServer(resourceServer -> resourceServer.jwt(Customizer.withDefaults()))
                .csrf(csrf -> csrf.ignoringRequestMatchers(endpointsMatcher))
                .with(authorizationServerConfigurer, Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer("http://scm-auth-server")
                .build();
    }
}

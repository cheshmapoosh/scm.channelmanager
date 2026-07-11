package ir.daneshrefah.scm.uaa.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.HazelcastInstance;
//import ir.daneshrefah.scm.cache.client.connector.CacheTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.HazelcastInstance;
import ir.daneshrefah.scm.cache.client.connector.CacheTemplate;
import ir.daneshrefah.scm.uaa.common.core.SessionCache;
import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalAuthenticationDetailsSource;
import ir.daneshrefah.scm.uaa.common.service.LogoutService;
import ir.daneshrefah.scm.uaa.security.TerminalUrlAuthenticationFailureHandler;
import ir.daneshrefah.scm.uaa.security.authenticationProvider.*;
import ir.daneshrefah.scm.uaa.security.converter.*;
import ir.daneshrefah.scm.uaa.security.filter.CaptchaVerifyFilter;
import ir.daneshrefah.scm.uaa.security.filter.MissingGrantTypeFallbackFilter;
import ir.daneshrefah.scm.uaa.service.shahkar.ShahkarOwnershipService;
import ir.daneshrefah.scm.uaa.service.user.OtpUserService;
import ir.daneshrefah.scm.uaa.service.user.UserService;
import ir.daneshrefah.scm.uaa.utils.Urls;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.*;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.MessageDigestPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2RefreshTokenAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-13
 */
@Configuration
@EnableWebSecurity
@Slf4j
@EnableMethodSecurity
@RequiredArgsConstructor
@EnableConfigurationProperties(PwaAuthenticationConfigProperties.class)
public class SecurityConfig {

    private static final String LOGIN_PROCESS_URI = "/login";


    private final CorsConfigurationSource configurationSource;
    private final LogoutSuccessHandler LogoutSuccessHandlerConfiguration;
    private final LogoutService logoutService;
    private final CacheManager cacheManager;
    private final UserService userService;
    private final OtpUserService otpUserService;
    private final ShahkarOwnershipService shahkarOwnershipService;


    @Qualifier("hazelcastClient")
    private final HazelcastInstance instance;

    @Value("${scm.super-app.session-ttl}")
    private  Long sessionTTL;


    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http,
                                                                      OAuth2GeneralAuthenticationProvider oAuth2GeneralAuthenticationProvider,
                                                                      OAuth2SmsOtpAuthenticationProvider oAuth2SmsOtpAuthenticationProvider,
                                                                      OAuth2ShahkarAuthenticationProvider oAuth2ShahkarAuthenticationProvider)
            throws Exception {

        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer();
        RequestMatcher endpointsMatcher = authorizationServerConfigurer.getEndpointsMatcher();
        authorizationServerConfigurer
                .authorizationEndpoint(authorizationEndpoint ->
                        authorizationEndpoint.consentPage("/consent"))
                .tokenEndpoint(tokenEndpoint ->
                                tokenEndpoint
                                        .accessTokenRequestConverters(
                                                converters -> converters.addAll(
                                                        Arrays.asList(new FirstPasswordGrantAuthenticationConverter(),
                                                                new SecondPasswordGrantAuthenticationConverter(),
                                                                new SmsOtpGrantAuthenticationConverter(),
                                                                new ShahkarGrantAuthenticationConverter(otpUserService,shahkarOwnershipService,instance,sessionTTL),
                                                                new DefaultGrantAuthenticationConverter()))
                                        )
                                        .authenticationProvider(oAuth2GeneralAuthenticationProvider)
                                        .authenticationProvider(oAuth2SmsOtpAuthenticationProvider)
                                        .authenticationProvider(oAuth2ShahkarAuthenticationProvider)
                )
                .oidc(Customizer.withDefaults());// Enable OpenID Connect 1.0

        http
                .addFilterBefore(new MissingGrantTypeFallbackFilter(), BasicAuthenticationFilter.class)
                .securityMatcher(endpointsMatcher)
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers(Urls.OAUTH2_TOKEN).permitAll()
                        .requestMatchers("/otp/public/**").permitAll()
                        .requestMatchers("/api/access-token/get-first-password-token").permitAll()
                        .anyRequest().authenticated()
                )
                // Redirect to the login page when not authenticated from the
                // authorization endpoint
                .exceptionHandling((exceptions) -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint(LOGIN_PROCESS_URI),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )
                )
                // Accept access tokens for User Info and/or Client Registration
                .oauth2ResourceServer((resourceServer) -> resourceServer
                        .jwt(Customizer.withDefaults()))
                .csrf(csrf -> csrf.ignoringRequestMatchers(endpointsMatcher))
                .with(authorizationServerConfigurer,Customizer.withDefaults());

        return http.build();
    }


    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http,
                                                          JwtDecoder jwtDecoder,
                                                          GeneralAuthenticationProvider generalAuthenticationProvider)
            throws Exception {
        AuthenticationFailureHandler failureHandler = failureHandler();
        JwtAuthenticationProvider jwtAuthenticationProvider = new JwtAuthenticationProvider(jwtDecoder, logoutService, cacheManager, userService);
        http
                .securityMatcher("/api/**", "/oauth2/**","/logout")
                .authenticationProvider(generalAuthenticationProvider)
                .authenticationManager(new ProviderManager(List.of(jwtAuthenticationProvider, generalAuthenticationProvider)))
                .authorizeHttpRequests((authorize) -> authorize
                                .requestMatchers("/error").permitAll()
                                .requestMatchers("/public/**").permitAll()
                                .requestMatchers("/otp/public/**").permitAll()
                                .requestMatchers("/oauth/token_key").permitAll()
                                .requestMatchers("/auth/login").permitAll()
                                .requestMatchers("/auth/refresh").permitAll()
                                .requestMatchers("/login**").permitAll()
                                .requestMatchers("/assets/**").permitAll()
                                .requestMatchers("/api/register").permitAll()
                                .requestMatchers("/api/valid").permitAll()
                                .requestMatchers("/api/client/version/checkAppVersion").permitAll()
                                .requestMatchers("/api/access-token/get-first-password-token").permitAll()
                                .anyRequest().authenticated()
                )
                .addFilterBefore(captchaVerifyFilter(failureHandler), UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(bearerAuthenticationFilter(http), UsernamePasswordAuthenticationFilter.class)
                // Form login handles the redirect to the login page from the
                // authorization server filter chain
                .exceptionHandling((exceptions) -> exceptions
                        .accessDeniedHandler(new AccessDeniedHandler() {
                            @Override
                            public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
                            }
                        })
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint(LOGIN_PROCESS_URI),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )
                        .defaultAuthenticationEntryPointFor(
                                new BearerTokenAuthenticationEntryPoint(),
                                new MediaTypeRequestMatcher(MediaType.APPLICATION_JSON)
                        )
                )
                .csrf(csrf -> csrf.ignoringRequestMatchers("/**"))
                .cors(httpSecurityCorsConfigurer -> {
                    httpSecurityCorsConfigurer.configurationSource(configurationSource);
                })
                .logout(logout -> {
                    logout.logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"));
                    logout.logoutSuccessHandler(LogoutSuccessHandlerConfiguration);
                    logout.invalidateHttpSession(true);
                    logout.deleteCookies("JSESSIONID");
                    logout.clearAuthentication(true);
                })
                .formLogin(login -> {
                    login.usernameParameter("username");
                    login.passwordParameter("password");
                    login.loginPage(LOGIN_PROCESS_URI);
                    login.failureHandler(failureHandler);
                    login.authenticationDetailsSource(new TerminalAuthenticationDetailsSource());
                });
        return http.build();
    }

    private AuthenticationFailureHandler failureHandler() {
        return new TerminalUrlAuthenticationFailureHandler(LOGIN_PROCESS_URI + "?error");
    }

    private CaptchaVerifyFilter captchaVerifyFilter(AuthenticationFailureHandler failureHandler) {
        return new CaptchaVerifyFilter(LOGIN_PROCESS_URI, failureHandler);
    }

    private BearerTokenAuthenticationFilter bearerAuthenticationFilter(HttpSecurity http) {
        BearerTokenAuthenticationFilter filter = new BearerTokenAuthenticationFilter((AuthenticationManagerResolver<HttpServletRequest>) context -> http.getSharedObject(AuthenticationManager.class));
        filter.setAuthenticationDetailsSource(new TerminalAuthenticationDetailsSource());
        return filter;
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer("http://scm-auth-server")
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        PasswordEncoder defaultPasswordEncoder = new MessageDigestPasswordEncoder("MD5");
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("noop", NoOpPasswordEncoder.getInstance());
        encoders.put("MD5", defaultPasswordEncoder);
        DelegatingPasswordEncoder passwordEncoder = new DelegatingPasswordEncoder("MD5", encoders);
        passwordEncoder.setDefaultPasswordEncoderForMatches(defaultPasswordEncoder);
        return passwordEncoder;
    }

    @Bean
    public AuthenticationTrustResolver authenticationTrustResolver() {
        return new AuthenticationTrustResolverImpl();
    }

    @Bean
    @Profile({"dev", "default", "test", "prod"})
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        log.info(">>> CORS DEACTIVATED ON ENVIRONMENT");
        return source;
    }


    @Bean
    public SessionCache sessionCache(CacheManager cacheManager) {
        return new SessionCache(cacheManager);
    }

}

package ir.daneshrefah.scm.uaa.config;

import ir.daneshrefah.scm.cache.client.connector.CacheTemplate;
import ir.daneshrefah.scm.uaa.common.core.SessionCache;
import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalAuthenticationDetailsSource;
import ir.daneshrefah.scm.uaa.common.token.JwtTokenConverter;
import ir.daneshrefah.scm.uaa.security.TerminalUrlAuthenticationFailureHandler;
import ir.daneshrefah.scm.uaa.security.authenticationProvider.GeneralAuthenticationProvider;
import ir.daneshrefah.scm.uaa.security.authenticationProvider.JwtAuthenticationProvider;
import ir.daneshrefah.scm.uaa.security.authenticationProvider.OAuth2GeneralAuthenticationProvider;
import ir.daneshrefah.scm.uaa.security.authenticationProvider.OAuth2SmsOtpAuthenticationProvider;
import ir.daneshrefah.scm.uaa.security.converter.FirstPasswordGrantAuthenticationConverter;
import ir.daneshrefah.scm.uaa.security.converter.SecondPasswordGrantAuthenticationConverter;
import ir.daneshrefah.scm.uaa.security.converter.ShahkarGrantAuthenticationConverter;
import ir.daneshrefah.scm.uaa.security.converter.SmsOtpGrantAuthenticationConverter;
import ir.daneshrefah.scm.uaa.security.filter.CaptchaVerifyFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

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

    private static final String LOGIN_PROCESS_URI = "/login";

//    @Autowired
//    private UserDetailsService userDetailsService;

    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http,
                                                                      OAuth2GeneralAuthenticationProvider oAuth2GeneralAuthenticationProvider,
                                                                      OAuth2SmsOtpAuthenticationProvider oAuth2SmsOtpAuthenticationProvider)
            throws Exception {

        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                new OAuth2AuthorizationServerConfigurer();
        RequestMatcher endpointsMatcher = authorizationServerConfigurer
                .getEndpointsMatcher();

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
                                                    new ShahkarGrantAuthenticationConverter()))
                                )
//                                .authenticationProviders(authenticationProviders -> {
//                                    authenticationProviders.add(oAuth2GeneralAuthenticationProvider);
//                                    authenticationProviders.add(oAuth2SmsOtpAuthenticationProvider);
//                                })
                                .authenticationProvider(oAuth2GeneralAuthenticationProvider)
                                .authenticationProvider(oAuth2SmsOtpAuthenticationProvider)
                )
                .oidc(Customizer.withDefaults());	// Enable OpenID Connect 1.0

        http
                .securityMatcher(endpointsMatcher)
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/oauth2/token").permitAll()
                        .requestMatchers("/otp/public/**").permitAll()
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
                .apply(authorizationServerConfigurer);

//        /*http

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http,
                                                          JwtDecoder jwtDecoder,
                                                          GeneralAuthenticationProvider generalAuthenticationProvider)
            throws Exception {
        AuthenticationFailureHandler failureHandler = failureHandler();
        JwtAuthenticationProvider jwtAuthenticationProvider = new JwtAuthenticationProvider(jwtDecoder);
        http
//                .authenticationProvider(generalAuthenticationProvider)
                .authenticationManager(new ProviderManager(List.of(jwtAuthenticationProvider, generalAuthenticationProvider)))
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/public/**").permitAll()
                        .requestMatchers("/otp/public/**").permitAll()
                        .requestMatchers("/login**").permitAll()
                        .requestMatchers("/assets/**").permitAll()

//                        .requestMatchers("/oauth2/token").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(captchaVerifyFilter(failureHandler), UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(bearerAuthenticationFilter(http), UsernamePasswordAuthenticationFilter.class)
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
                                new LoginUrlAuthenticationEntryPoint(LOGIN_PROCESS_URI),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )
                        .defaultAuthenticationEntryPointFor(
                                new BearerTokenAuthenticationEntryPoint(),
                                new MediaTypeRequestMatcher(MediaType.APPLICATION_JSON)
                        )
                )
                .csrf(csrf -> csrf.ignoringRequestMatchers("/**"))
                .logout(logout -> {
//                    logout.logoutUrl("/logout");
                    logout.logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"));
                })
                .formLogin(login -> {
//                    login.setFormLoginEnabled(true);
                    login.usernameParameter("username");
                    login.passwordParameter("password");
                    login.loginPage(LOGIN_PROCESS_URI);
                    login.failureHandler(failureHandler);
//                    login.failureUrl("/login?error");
                    login.authenticationDetailsSource(new TerminalAuthenticationDetailsSource());
//                    login.setAuthenticationUrl(getLoginProcessingUrl());
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
        PasswordEncoder defaultPasswordEncoder = new org.springframework.security.crypto.password.MessageDigestPasswordEncoder("MD5");
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("noop", org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance());
        encoders.put("MD5", defaultPasswordEncoder);
        DelegatingPasswordEncoder passwordEncoder = new DelegatingPasswordEncoder("MD5", encoders);
        passwordEncoder.setDefaultPasswordEncoderForMatches(defaultPasswordEncoder);
        return passwordEncoder;
    }

    @Bean
    public SessionCache sessionCache(CacheTemplate cacheTemplate) {
        return new SessionCache(cacheTemplate);
    }

}

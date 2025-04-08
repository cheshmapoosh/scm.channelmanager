package ir.daneshrefah.scm.uaa.config;

import ir.daneshrefah.scm.cache.client.connector.CacheTemplate;
import ir.daneshrefah.scm.uaa.common.core.SessionCache;
import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalAuthenticationDetailsSource;
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
import ir.daneshrefah.scm.utils.string.StringUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
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
public class SecurityConfig {

    private static final String LOGIN_PROCESS_URI = "/login";

    //    @Autowired
//    private UserDetailsService userDetailsService;
    @Autowired
    private CorsConfigurationSource configurationSource;

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
                .oidc(Customizer.withDefaults());// Enable OpenID Connect 1.0

        http
                .securityMatcher(endpointsMatcher)
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/oauth2/token").permitAll()
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
                                .requestMatchers("/api/access-token/get-first-password-token").permitAll()

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
                                System.out.print("");
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
                    logout.logoutSuccessHandler(this::logoutSuccessHandlerConfiguration);
                    logout.invalidateHttpSession(true);
                    logout.deleteCookies("JSESSIONID");
                    logout.clearAuthentication(true);
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
    public AuthenticationTrustResolver authenticationTrustResolver() {
        return new AuthenticationTrustResolverImpl();
    }

    @Bean
    @Profile({"dev","default","test","prod"})
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
    public SessionCache sessionCache(CacheTemplate cacheTemplate) {
        return new SessionCache(cacheTemplate);
    }

    private void logoutSuccessHandlerConfiguration(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        String redirectUri = request.getParameter("redirect_uri");
        String clientId = request.getParameter("client_id");
        String responseType = request.getParameter("response_type");
        String scope = request.getParameter("scope");
        response.setStatus(HttpServletResponse.SC_OK);
        if (!(StringUtils.isEmpty(responseType) || StringUtils.isEmpty(redirectUri) || StringUtils.isEmpty(clientId) || StringUtils.isEmpty(scope))) {
            String serverHost = request.getRequestURL().toString().split("/logout")[0];
            String redirection = serverHost +
                                 "/oauth2/authorize?response_type=" + responseType +
                                 "&client_id=" + clientId +
                                 "&redirect_uri=" + redirectUri +
                                 "&scope=" + scope;
            response.sendRedirect(redirection);
        }
    }

}

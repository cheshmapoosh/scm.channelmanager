package ir.daneshrefah.scm.uaa.config;

import ir.daneshrefah.scm.uaa.common.service.LogoutService;
import ir.daneshrefah.scm.uaa.security.TerminalUrlAuthenticationFailureHandler;
import ir.daneshrefah.scm.uaa.security.filter.CaptchaVerifyFilter;
import ir.daneshrefah.scm.uaa.security.form.UaaFormLoginAuthenticationProvider;
import ir.daneshrefah.scm.uaa.security.resource.JtiValidatingBearerAuthenticationProvider;
import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalAuthenticationDetailsSource;
import ir.daneshrefah.scm.uaa.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.web.cors.CorsConfigurationSource;

import java.io.IOException;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class ResourceServerSecurityConfig {
    private static final String LOGIN_PROCESS_URI = "/login";

    private final CorsConfigurationSource configurationSource;
    private final LogoutSuccessHandler LogoutSuccessHandlerConfiguration;
    private final LogoutService logoutService;
    private final CacheManager cacheManager;
    private final UserService userService;

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(
            HttpSecurity http,
            JwtDecoder jwtDecoder,
            UaaFormLoginAuthenticationProvider formLoginAuthenticationProvider
    ) throws Exception {
        AuthenticationFailureHandler failureHandler = failureHandler();
        JtiValidatingBearerAuthenticationProvider bearerAuthenticationProvider = new JtiValidatingBearerAuthenticationProvider(
                jwtDecoder,
                logoutService,
                cacheManager,
                userService
        );
        http
                .securityMatcher("/api/**", "/oauth2/**", "/logout")
                .authenticationProvider(formLoginAuthenticationProvider)
                .authenticationManager(new ProviderManager(List.of(bearerAuthenticationProvider, formLoginAuthenticationProvider)))
                .authorizeHttpRequests(authorize -> authorize
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
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedHandler(this::handleAccessDenied)
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
                .cors(cors -> cors.configurationSource(configurationSource))
                .logout(logout -> {
                    logout.logoutRequestMatcher(PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.GET, "/logout"));
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
        BearerTokenAuthenticationFilter filter = new BearerTokenAuthenticationFilter(
                (AuthenticationManagerResolver<jakarta.servlet.http.HttpServletRequest>)
                        context -> http.getSharedObject(AuthenticationManager.class)
        );
        filter.setAuthenticationDetailsSource(new TerminalAuthenticationDetailsSource());
        return filter;
    }

    private void handleAccessDenied(
            jakarta.servlet.http.HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response,
            AccessDeniedException exception
    ) throws IOException {
        response.sendError(jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN, "access_denied");
    }
}

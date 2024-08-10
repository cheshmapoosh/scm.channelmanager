package ir.daneshrefah.scm.config.server.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Value("${scm.config.scm-user.name}")
    private String scmUserName;
    @Value("${scm.config.scm-user.pass}")
    private String scmUserPass;
    @Value("${scm.config.cfg-user.name}")
    private String cfgUserName;
    @Value("${scm.config.cfg-user.name}")
    private String cfgUserPass;
    @Value("${scm.config.opr-user.name}")
    private String oprUserName;
    @Value("${scm.config.opr-user.name}")
    private String oprUserPass;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorizationManagerRequestMatcherRegistry -> {
                    authorizationManagerRequestMatcherRegistry
                            .anyRequest()
                            .fullyAuthenticated();
                })
                .httpBasic(httpBasicCustomizer ->
                        httpBasicCustomizer
                                .realmName("Config Server")
                                .authenticationEntryPoint((request, response, authException) -> {
                                    response.addHeader("WWW-Authenticate", "Basic realm=\"Config Server\"");
                                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
                                })
                ).csrf(AbstractHttpConfigurer::disable) ;
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        manager.createUser(User.withDefaultPasswordEncoder()
                .username(scmUserName)
                .password(scmUserPass)
                .roles("ADMIN")
                .build());
        manager.createUser(User.withDefaultPasswordEncoder()
                .username(cfgUserName)
                .password(cfgUserPass)
                .roles("CONFIG")
                .build());
        manager.createUser(User.withDefaultPasswordEncoder()
                .username(oprUserName)
                .password(oprUserPass)
                .roles("OPERATOR")
                .build());
        return manager;
    }
}
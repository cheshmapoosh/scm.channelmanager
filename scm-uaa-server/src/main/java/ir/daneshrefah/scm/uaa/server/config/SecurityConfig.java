package ir.daneshrefah.scm.uaa.server.config;

import ir.daneshrefah.scm.uaa.server.security.crypto.password.Md5UsernamePasswordEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new Md5UsernamePasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager(
            User.withUsername("user")
                .password("42f4f9be9450cd4a6699b3ecd3c37061")
                .roles("USER")
                .build()
        );
    }
}

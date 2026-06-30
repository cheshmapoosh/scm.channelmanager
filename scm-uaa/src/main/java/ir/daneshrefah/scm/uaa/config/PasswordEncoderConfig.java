package ir.daneshrefah.scm.uaa.config;

import ir.daneshrefah.scm.uaa.security.password.LegacyUsernameSaltedMd5PasswordEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;

@Configuration
@SuppressWarnings("removal")
public class PasswordEncoderConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        PasswordEncoder legacyMd5 = new LegacyUsernameSaltedMd5PasswordEncoder();
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("legacy-md5", legacyMd5);
        encoders.put("MD5", legacyMd5);
        encoders.put("noop", NoOpPasswordEncoder.getInstance());
        DelegatingPasswordEncoder passwordEncoder = new DelegatingPasswordEncoder("legacy-md5", encoders);
        passwordEncoder.setDefaultPasswordEncoderForMatches(legacyMd5);
        return passwordEncoder;
    }

    @Bean
    public AuthenticationTrustResolver authenticationTrustResolver() {
        return new AuthenticationTrustResolverImpl();
    }
}

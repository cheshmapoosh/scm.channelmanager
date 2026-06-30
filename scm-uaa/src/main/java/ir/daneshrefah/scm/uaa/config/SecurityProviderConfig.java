package ir.daneshrefah.scm.uaa.config;

import ir.daneshrefah.scm.uaa.security.authentication.method.LoginAuthenticationMethodProviderSupport;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.authentication.ProviderManager;

import java.util.List;

@Configuration
public class SecurityProviderConfig {
    @Bean
    public AuthenticationManager authenticationManager(
            List<LoginAuthenticationMethodProviderSupport> providers,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        List<AuthenticationProvider> authenticationProviders = providers.stream()
                .map(AuthenticationProvider.class::cast)
                .toList();
        ProviderManager providerManager = new ProviderManager(authenticationProviders);
        providerManager.setAuthenticationEventPublisher(new DefaultAuthenticationEventPublisher(applicationEventPublisher));
        return providerManager;
    }
}

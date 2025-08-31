package ir.daneshrefah.scm.core.authority.decision.manager;

import ir.daneshrefah.scm.core.authority.decision.voter.SecurityProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class SecurityChainConfiguration {

    @Bean
    public Map<Class<?>, SecurityProvider> securityProviders(List<SecurityProvider> providers) {
        return providers.stream()
                .collect(Collectors.toMap(SecurityProvider::getClass, p -> p));
    }

}

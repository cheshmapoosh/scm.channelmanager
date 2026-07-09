package ir.daneshrefah.scm.uaa.starter.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.uaa.starter.core.AuthenticationClientTemplate;
import ir.daneshrefah.scm.uaa.starter.filter.BearerAuthenticationFilter;
import ir.daneshrefah.scm.uaa.starter.provider.AbstractClientAuthenticationProvider;
import ir.daneshrefah.scm.uaa.starter.provider.AnonymousAuthenticationProvider;
import ir.daneshrefah.scm.uaa.starter.provider.BasicRemoteAuthenticationProvider;
import ir.daneshrefah.scm.uaa.starter.provider.BearerAuthenticationProvider;
import ir.daneshrefah.scm.uaa.starter.provider.ClaimRemoteAuthenticationProvider;
import ir.daneshrefah.scm.uaa.starter.provider.ClientRemoteAuthenticationProvider;
import ir.daneshrefah.scm.uaa.starter.provider.SessionAuthenticationProvider;
import ir.daneshrefah.scm.uaa.starter.remote.LocalSecurityServiceProvider;
import ir.daneshrefah.scm.uaa.starter.remote.RemoteSecurityServiceProvider;
import ir.daneshrefah.scm.uaa.starter.remote.SecurityServiceProvider;
import ir.daneshrefah.scm.uaa.common.core.SessionCache;
import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalAuthenticationDetailsSource;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@AutoConfiguration(after = ScmSecurityCacheAutoConfiguration.class)
@ConditionalOnProperty(
        prefix = "scm.security.legacy-authentication",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class ScmLegacyAuthenticationProvidersAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(RemoteSecurityServiceProvider.class)
    @ConditionalOnProperty(name = "scm.security.distributed", havingValue = "true", matchIfMissing = false)
    public RemoteSecurityServiceProvider remoteSecurityServiceProvider(
            RestTemplate restTemplate,
            JwtDecoder jwtDecoder
    ) {
        return new RemoteSecurityServiceProvider(restTemplate, jwtDecoder);
    }

    @Bean
    @ConditionalOnMissingBean(SecurityServiceProvider.class)
    @ConditionalOnProperty(name = "scm.security.distributed", havingValue = "false", matchIfMissing = true)
    public LocalSecurityServiceProvider localSecurityServiceProvider() {
        return new LocalSecurityServiceProvider();
    }

    @Bean
    @ConditionalOnMissingBean
    public AnonymousAuthenticationProvider anonymousAuthenticationProvider(
            SessionCache sessionCache,
            CacheManager cacheManager
    ) {
        return new AnonymousAuthenticationProvider(sessionCache, cacheManager);
    }

    @Bean
    @ConditionalOnMissingBean
    public SessionAuthenticationProvider sessionAuthenticationProvider(
            SessionCache sessionCache,
            CacheManager cacheManager
    ) {
        return new SessionAuthenticationProvider(sessionCache, cacheManager);
    }

    @Bean
    @ConditionalOnMissingBean
    public BearerAuthenticationProvider bearerAuthenticationProvider(
            JwtDecoder jwtDecoder,
            SessionCache sessionCache,
            CacheManager cacheManager
    ) {
        return new BearerAuthenticationProvider(jwtDecoder, sessionCache, cacheManager);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(RemoteSecurityServiceProvider.class)
    public BasicRemoteAuthenticationProvider basicRemoteAuthenticationProvider(
            RemoteSecurityServiceProvider remoteSecurityServiceProvider,
            SessionCache sessionCache,
            CacheManager cacheManager
    ) {
        return new BasicRemoteAuthenticationProvider(remoteSecurityServiceProvider, sessionCache, cacheManager);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(RemoteSecurityServiceProvider.class)
    @ConditionalOnProperty(
            prefix = "scm.security.legacy-authentication.claim",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = false
    )
    @SuppressWarnings("removal")
    public ClaimRemoteAuthenticationProvider claimRemoteAuthenticationProvider(
            RemoteSecurityServiceProvider remoteSecurityServiceProvider,
            ObjectMapper objectMapper,
            SessionCache sessionCache,
            CacheManager cacheManager
    ) {
        return new ClaimRemoteAuthenticationProvider(remoteSecurityServiceProvider, objectMapper, sessionCache, cacheManager);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(RemoteSecurityServiceProvider.class)
    public ClientRemoteAuthenticationProvider clientRemoteAuthenticationProvider(
            RemoteSecurityServiceProvider remoteSecurityServiceProvider,
            JwtDecoder jwtDecoder,
            ObjectMapper objectMapper,
            SessionCache sessionCache,
            CacheManager cacheManager
    ) {
        return new ClientRemoteAuthenticationProvider(remoteSecurityServiceProvider, jwtDecoder, objectMapper, sessionCache, cacheManager);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthenticationManager legacyAuthenticationManager(ApplicationContext context) {
        Map<String, AbstractClientAuthenticationProvider> providersMap =
                context.getBeansOfType(AbstractClientAuthenticationProvider.class);
        List<AuthenticationProvider> providers = providersMap.values().stream()
                .map(AuthenticationProvider.class::cast)
                .toList();
        return new ProviderManager(providers);
    }

    @Bean
    @ConditionalOnMissingBean
    public TerminalAuthenticationDetailsSource terminalAuthenticationDetailsSource() {
        return new TerminalAuthenticationDetailsSource();
    }

    @Bean
    @ConditionalOnMissingBean
    public BearerAuthenticationFilter bearerAuthenticationFilter(
            AuthenticationManager authenticationManager,
            TerminalAuthenticationDetailsSource authenticationDetailsSource
    ) {
        BearerAuthenticationFilter filter = new BearerAuthenticationFilter(authenticationManager);
        filter.setAuthenticationDetailsSource(authenticationDetailsSource);
        return filter;
    }

    @Bean

    public AuthenticationClientTemplate authenticationClientTemplate(
            AuthenticationManager authenticationManager,
            SecurityServiceProvider securityServiceProvider
    ) {
        return new AuthenticationClientTemplate(authenticationManager, securityServiceProvider);
    }
}

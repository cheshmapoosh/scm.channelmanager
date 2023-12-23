package ir.daneshrefah.scm.uaa.client.autoconfigure;

import ir.daneshrefah.scm.cache.client.connector.CacheTemplate;
import ir.daneshrefah.scm.uaa.client.core.AuthenticationClientTemplate;
import ir.daneshrefah.scm.uaa.client.provider.AbstractClientAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-19
 */
@AutoConfiguration
@EnableWebSecurity
@ConditionalOnWebApplication
public class AuthenticationClientAutoConfiguration {

    /*@Autowired
    private CacheTemplate cacheTemplate;*/
//    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
//    private String publicKeyUrl;
//
//    @Value("${uaa.server.token-endpoint}")
//    private String tokenEndpoint;
    private String ANONYMOUS_AUTH_KEY = UUID.randomUUID().toString();
    @Autowired
    private CacheTemplate cacheTemplate;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, ApplicationContext context) throws Exception {
        http
                .authorizeHttpRequests((authorize) -> authorize
                        .anyRequest().authenticated()
                )
//                .addFilter(anonymousAuthenticationFilter())
                .addFilter(basicAuthenticationFilter(context))
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwtConfigurer -> jwtConfigurer.decoder(jwtDecoder())));

        return http.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri("http://localhost:8000/oauth2/jwks").build();
    }

    @Bean
    public AuthenticationManager authenticationManager(ApplicationContext context) {
        Map<String, AbstractClientAuthenticationProvider> providersMap =
                context.getBeansOfType(AbstractClientAuthenticationProvider.class);
        List<AuthenticationProvider> providers = providersMap.entrySet().stream()
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
        return new ProviderManager(providers
//                new AnonymousAuthenticationProvider(ANONYMOUS_AUTH_KEY),
//                new BearerAuthenticationProvider(),
//                new SessionAuthenticationProvider(),
//                new BasicAuthenticationProvider()
        );
    }

    @Bean
    public BasicAuthenticationFilter basicAuthenticationFilter(ApplicationContext context) {
        return new BasicAuthenticationFilter(authenticationManager(context));
    }

    /*@Bean
    public AnonymousAuthenticationFilter anonymousAuthenticationFilter() {
        return new AnonymousAuthenticationFilter(ANONYMOUS_AUTH_KEY);
    }*/

    @Bean
    public AuthenticationClientTemplate authenticationClientTemplate(AuthenticationManager authenticationManager) {
        return new AuthenticationClientTemplate(authenticationManager);
    }

    /*@Bean
    public UaaServerConnectorService uaaServerConnectorService(RestTemplate restTemplate){
        return new UaaServerConnectorService(restTemplate,tokenEndpoint);
    }*/


   /* @Bean
    public RestTemplate restTemplate()
    {
        return new RestTemplate();
    }*/

    /*@Bean
    @ConditionalOnClass(ManagedChannel.class)
    public ManagedChannel managedChannel()
    {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8000)
                .usePlaintext()
                .build();
        return channel;
    }*/

}

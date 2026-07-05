package ir.daneshrefah.scm.uaa.client.autoconfigure;

import ir.daneshrefah.scm.common.event.ScmEventPublisher;
import ir.daneshrefah.scm.common.event.SpringScmEventPublisher;
import ir.daneshrefah.scm.uaa.client.properties.ScmResourceServerProperties;
import ir.daneshrefah.scm.uaa.client.resource.ScmBearerTokenResolver;
import ir.daneshrefah.scm.uaa.client.security.ScmJwtAuthenticationConverter;
import ir.daneshrefah.scm.uaa.client.security.event.ScmAuthenticationSuccessEventListener;
import ir.daneshrefah.scm.uaa.client.security.event.ScmPublishingAccessDeniedHandler;
import ir.daneshrefah.scm.uaa.client.security.event.ScmPublishingAuthenticationEntryPoint;
import ir.daneshrefah.scm.uaa.client.security.event.ScmSecurityEventPublishingFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@AutoConfiguration
@ConditionalOnClass(SecurityFilterChain.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(ScmResourceServerProperties.class)
public class ScmResourceServerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public Converter<Jwt, UsernamePasswordAuthenticationToken> scmJwtAuthenticationConverter(ScmResourceServerProperties properties) {
        return new ScmJwtAuthenticationConverter(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public BearerTokenResolver scmBearerTokenResolver(ScmResourceServerProperties properties) {
        return new ScmBearerTokenResolver(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public ScmEventPublisher scmEventPublisher(ApplicationEventPublisher publisher) {
        return new SpringScmEventPublisher(publisher);
    }

    @Bean
    @ConditionalOnMissingBean
    public ScmAuthenticationSuccessEventListener scmAuthenticationSuccessEventListener(ScmEventPublisher eventPublisher) {
        return new ScmAuthenticationSuccessEventListener(eventPublisher);
    }

    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    @ConditionalOnProperty(
            prefix = "scm.security.resource-server",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    public SecurityFilterChain scmResourceServerSecurityFilterChain(
            HttpSecurity http,
            ScmResourceServerProperties properties,
            Converter<Jwt, UsernamePasswordAuthenticationToken> scmJwtAuthenticationConverter,
            BearerTokenResolver bearerTokenResolver,
            ScmEventPublisher eventPublisher
    ) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.exceptionHandling(exception -> exception
                .authenticationEntryPoint(new ScmPublishingAuthenticationEntryPoint(eventPublisher, properties))
                .accessDeniedHandler(new ScmPublishingAccessDeniedHandler(eventPublisher, properties)));
        http.authorizeHttpRequests(authorize -> {
            for (String publicPath : properties.getPublicPaths()) {
                if (StringUtils.hasText(publicPath)) {
                    authorize.requestMatchers(publicPath.trim()).permitAll();
                }
            }
            authorize.anyRequest().authenticated();
        });
        http.oauth2ResourceServer(oauth2 -> oauth2
                .bearerTokenResolver(bearerTokenResolver)
                .jwt(jwt -> jwt.jwtAuthenticationConverter(scmJwtAuthenticationConverter)));
        http.addFilterBefore(
                new ScmSecurityEventPublishingFilter(eventPublisher, properties),
                BearerTokenAuthenticationFilter.class
        );
        return http.build();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            prefix = "scm.security.resource-server",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    public JwtDecoder scmJwtDecoder(ScmResourceServerProperties properties) {
        JwtDecoder decoder = createDecoder(properties);
        OAuth2TokenValidator<Jwt> validator = validator(properties);
        if (decoder instanceof NimbusJwtDecoder nimbusJwtDecoder) {
            nimbusJwtDecoder.setJwtValidator(validator);
        }
        return decoder;
    }

    private JwtDecoder createDecoder(ScmResourceServerProperties properties) {
        if (StringUtils.hasText(properties.getJwkSetUri())) {
            return NimbusJwtDecoder.withJwkSetUri(properties.getJwkSetUri().trim()).build();
        }
        if (StringUtils.hasText(properties.getIssuerUri())) {
            return JwtDecoders.fromIssuerLocation(properties.getIssuerUri().trim());
        }
        throw new IllegalStateException("SCM resource server requires scm.security.resource-server.issuer-uri or jwk-set-uri");
    }

    private OAuth2TokenValidator<Jwt> validator(ScmResourceServerProperties properties) {
        List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
        if (StringUtils.hasText(properties.getIssuerUri())) {
            validators.add(JwtValidators.createDefaultWithIssuer(properties.getIssuerUri().trim()));
        } else {
            validators.add(JwtValidators.createDefault());
        }
        validators.add(jwt -> validateAudiences(jwt, properties.getAudiences()));
        validators.add(jwt -> validateRequiredClaims(jwt, properties.getRequiredClaims()));
        return token -> {
            for (OAuth2TokenValidator<Jwt> current : validators) {
                OAuth2TokenValidatorResult result = current.validate(token);
                if (result.hasErrors()) {
                    return result;
                }
            }
            return OAuth2TokenValidatorResult.success();
        };
    }

    private OAuth2TokenValidatorResult validateAudiences(
            Jwt jwt,
            List<String> expectedAudiences
    ) {
        List<String> configured = expectedAudiences == null
                ? List.of()
                : expectedAudiences.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .toList();
        if (configured.isEmpty() || jwt.getAudience().stream().anyMatch(configured::contains)) {
            return OAuth2TokenValidatorResult.success();
        }
        return failure("invalid_token", "JWT audience is not accepted by this SCM resource server");
    }

    private OAuth2TokenValidatorResult validateRequiredClaims(
            Jwt jwt,
            List<String> requiredClaims
    ) {
        if (requiredClaims == null) {
            return OAuth2TokenValidatorResult.success();
        }
        for (String requiredClaim : requiredClaims) {
            if (!StringUtils.hasText(requiredClaim)) {
                continue;
            }
            Object value = jwt.getClaim(requiredClaim.trim());
            if (value == null || value instanceof String text && !StringUtils.hasText(text)) {
                return failure("invalid_token", "JWT is missing required SCM claim: " + requiredClaim.trim());
            }
        }
        return OAuth2TokenValidatorResult.success();
    }

    private OAuth2TokenValidatorResult failure(
            String code,
            String description
    ) {
        return OAuth2TokenValidatorResult.failure(new OAuth2Error(code, description, null));
    }
}

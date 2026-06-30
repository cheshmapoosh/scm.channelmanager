package ir.daneshrefah.scm.uaa.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableConfigurationProperties(UaaCorsProperties.class)
@Slf4j
public class CorsSecurityConfig {
    @Bean("uaaCorsConfigurationSource")
    @ConditionalOnProperty(
            prefix = "scm.uaa.cors",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    public CorsConfigurationSource uaaCorsConfigurationSource(UaaCorsProperties properties) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(properties.getAllowedOriginPatterns());
        configuration.setAllowedMethods(properties.getAllowedMethods());
        configuration.setAllowedHeaders(properties.getAllowedHeaders());
        configuration.setExposedHeaders(properties.getExposedHeaders());
        configuration.setAllowCredentials(properties.isAllowCredentials());
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        properties.getPathPatterns().forEach(path -> source.registerCorsConfiguration(path, configuration));
        log.info("UAA CORS configuration initialized with {} origin pattern(s)",
                properties.getAllowedOriginPatterns().size());
        return source;
    }
}

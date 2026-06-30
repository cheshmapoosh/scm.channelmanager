package ir.daneshrefah.scm.uaa.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableConfigurationProperties(UaaCorsProperties.class)
@Slf4j
public class CorsSecurityConfig {
    @Bean
    @Profile({"dev", "default", "test", "prod"})
    public CorsConfigurationSource corsConfigurationSource(UaaCorsProperties properties) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(properties.getAllowedOriginPatterns());
        configuration.setAllowedMethods(properties.getAllowedMethods());
        configuration.setAllowedHeaders(properties.getAllowedHeaders());
        configuration.setAllowCredentials(properties.isAllowCredentials());
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        properties.getPathPatterns().forEach(path -> source.registerCorsConfiguration(path, configuration));
        log.info("CORS configuration initialized for UAA");
        return source;
    }
}

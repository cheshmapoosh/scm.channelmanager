package ir.daneshrefah.scm.core.config;

import ir.daneshrefah.scm.core.integration.gateway.HazelcastTtlIdempotentRepository;
import org.apache.camel.spi.IdempotentRepository;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RestGatewayIdempotencyConfig {

    public static final String REST_GATEWAY_IDEMPOTENT_REPOSITORY =
            "restGatewayIdempotentRepository";

    @Bean(REST_GATEWAY_IDEMPOTENT_REPOSITORY)
    public IdempotentRepository restGatewayIdempotentRepository(
            CacheManager cacheManager,
            RestGatewayIdempotencyProperties properties) {

        return new HazelcastTtlIdempotentRepository(
                cacheManager,
                properties.getMapName()
        );
    }
}
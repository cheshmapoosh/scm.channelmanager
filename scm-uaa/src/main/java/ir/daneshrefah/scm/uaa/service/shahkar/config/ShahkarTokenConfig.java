package ir.daneshrefah.scm.uaa.service.shahkar.config;

import com.hazelcast.core.HazelcastInstance;
import ir.daneshrefah.scm.uaa.service.shahkar.token.ShahkarTokenProvider;
import ir.daneshrefah.scm.uaa.service.shahkar.transport.ShahkarApiClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ShahkarTokenConfig {

    @Bean
    public ShahkarTokenProvider shahkarTokenProvider(
            HazelcastInstance hazelcast,
            ShahkarApiClient apiClient,
            ShahkarProperties props
    ) {
        return new ShahkarTokenProvider(hazelcast, apiClient, props);
    }
}
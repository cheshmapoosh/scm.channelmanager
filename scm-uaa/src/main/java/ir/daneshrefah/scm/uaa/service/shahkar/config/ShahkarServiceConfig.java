package ir.daneshrefah.scm.uaa.service.shahkar.config;

import ir.daneshrefah.scm.uaa.service.shahkar.ShahkarOwnershipService;
import ir.daneshrefah.scm.uaa.service.shahkar.token.ShahkarTokenProvider;
import ir.daneshrefah.scm.uaa.service.shahkar.transport.ShahkarApiClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;

@Configuration
public class ShahkarServiceConfig {

    @Bean
    public ShahkarOwnershipService shahkarOwnershipService(
            ShahkarTokenProvider tokenProvider,
            ShahkarApiClient apiClient,
            ExecutorService shahkarVirtualThreadExecutor
    ) {
        return new ShahkarOwnershipService(tokenProvider, apiClient, shahkarVirtualThreadExecutor);
    }
}
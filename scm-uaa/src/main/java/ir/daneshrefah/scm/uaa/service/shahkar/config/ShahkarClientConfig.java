package ir.daneshrefah.scm.uaa.service.shahkar.config;

import ir.daneshrefah.scm.uaa.service.shahkar.transport.ShahkarApiClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ShahkarClientConfig {

    @Bean
    public ShahkarApiClient shahkarApiClient(RestClient shahkarRestClient, ShahkarProperties props) {
        return new ShahkarApiClient(shahkarRestClient, props);
    }
}
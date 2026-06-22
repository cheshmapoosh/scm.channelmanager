package ir.daneshrefah.scm.cache.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.matcher.WildcardConfigPatternMatcher;
import com.hazelcast.core.HazelcastInstance;
import ir.daneshrefah.scm.cache.infrastructure.hazelcast.HazelcastBootstrap;
import ir.daneshrefah.scm.cache.infrastructure.hazelcast.HazelcastElementRiskProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(HazelcastElementRiskProperties.class)
@RequiredArgsConstructor
public class HazelcastConfiguration {

    @Bean
    @ConfigurationProperties(
            prefix = "scm.cache.hazelcast.member",
            ignoreUnknownFields = false
    )
    Config hazelcastMemberConfig() {
        Config config = new Config();
        config.setConfigPatternMatcher(new WildcardConfigPatternMatcher());
        return config;
    }

    @Bean(destroyMethod = "shutdown")
    HazelcastInstance hazelcastInstance(
            Config hazelcastMemberConfig,
            HazelcastBootstrap bootstrap
    ) {
        return bootstrap.start(hazelcastMemberConfig);
    }
}

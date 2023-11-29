package ir.daneshrefah.scm.cache.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.matcher.WildcardConfigPatternMatcher;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import ir.daneshrefah.scm.cache.config.instances.service.InstanceConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-11-19
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class HazelcastConfig {


    @Bean
    @ConfigurationProperties(prefix = "hazelcast.config", ignoreUnknownFields = false)
    Config config() {
        Config config = new Config();
        config.setConfigPatternMatcher(new WildcardConfigPatternMatcher());
        log.info(">>> hazelcast config loaded");
        return config;
    }

    @Bean
    public Boolean setupInstanceConfig(InstanceConfig instanceConfig){
        instanceConfig.setup(config());
        return true;
    }
    @Bean
    public HazelcastInstance hazelcastInstance(){
        return Hazelcast.newHazelcastInstance(config());
    }

}

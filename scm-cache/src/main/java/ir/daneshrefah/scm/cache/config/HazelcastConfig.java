package ir.daneshrefah.scm.cache.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.matcher.WildcardConfigPatternMatcher;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import ir.daneshrefah.scm.cache.common.Properties;
import ir.daneshrefah.scm.cache.config.instances.service.InstanceConfig;
import ir.daneshrefah.scm.cache.config.model.HazelConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
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

    private final Properties properties;
//    private final InstanceConfig instanceConfig;

    @Bean
    @ConfigurationProperties(prefix = "hazelcast.config", ignoreUnknownFields = false)
    HazelConfig hazelAddonConfig() {
        HazelConfig hazelConfig = new HazelConfig();
        log.info(">>> hazelcast config loaded");
        return hazelConfig;
    }

    @Bean
    public Config config(){
        Config config = hazelAddonConfig();
        config.setConfigPatternMatcher(new WildcardConfigPatternMatcher());
        return config;
    }

    @Bean
    public CommandLineRunner commandLineRunner(InstanceConfig instanceConfig){
     return (command) ->{
         instanceConfig.setup(config());
        };
    }
    @Bean
    public HazelcastInstance hazelcastInstance(){
        return Hazelcast.newHazelcastInstance(config());
    }

}

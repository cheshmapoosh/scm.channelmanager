package ir.daneshrefah.scm.cache.client.config;

import ir.daneshrefah.scm.cache.client.observation.CacheClientObservationSupport;
import ir.daneshrefah.scm.observation.ScmObservation;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = CacheClientAutoConfiguration.class)
@ConditionalOnBean(ScmObservation.class)
public class CacheClientObservationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CacheClientObservationSupport cacheClientObservationSupport(ScmObservation observation) {
        return new CacheClientObservationSupport(observation);
    }
}

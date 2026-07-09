package ir.daneshrefah.scm.cache.starter.config;

import com.hazelcast.core.HazelcastInstance;
import ir.daneshrefah.scm.cache.starter.connector.QueueTemplate;
import ir.daneshrefah.scm.cache.starter.connector.QueueTemplateImpl;
import ir.daneshrefah.scm.cache.starter.event.ScmCacheEventSupport;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = {CacheClientAutoConfiguration.class, CacheClientEventAutoConfiguration.class})
public class CacheClientQueueAutoConfiguration {

    @Bean
    @ConditionalOnBean(HazelcastInstance.class)
    @ConditionalOnMissingBean
    public QueueTemplate queueTemplate(HazelcastInstance hazelcastInstance, ScmCacheEventSupport cacheEventSupport) {
        return new QueueTemplateImpl(hazelcastInstance, cacheEventSupport);
    }
}

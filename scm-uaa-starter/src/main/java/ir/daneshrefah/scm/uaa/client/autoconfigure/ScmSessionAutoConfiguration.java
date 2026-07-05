package ir.daneshrefah.scm.uaa.client.autoconfigure;

import ir.daneshrefah.scm.uaa.client.session.DefaultScmSessionReader;
import ir.daneshrefah.scm.uaa.client.session.ScmSessionReader;
import ir.daneshrefah.scm.uaa.common.core.SessionCache;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = ScmSecurityCacheAutoConfiguration.class)
public class ScmSessionAutoConfiguration {

    @Bean
    @ConditionalOnBean(SessionCache.class)
    @ConditionalOnMissingBean
    public ScmSessionReader scmSessionReader(SessionCache sessionCache) {
        return new DefaultScmSessionReader(sessionCache);
    }
}

package ir.daneshrefah.scm.uaa.client.autoconfigure;

import ir.daneshrefah.scm.uaa.client.security.ScmOwnershipGuard;
import ir.daneshrefah.scm.uaa.client.security.ScmSecurityContext;
import ir.daneshrefah.scm.uaa.client.security.SpringSecurityScmSecurityContext;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.context.SecurityContextHolder;

@AutoConfiguration(after = ScmResourceServerAutoConfiguration.class)
@ConditionalOnClass(SecurityContextHolder.class)
public class ScmOwnershipAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ScmSecurityContext scmSecurityContext() {
        return new SpringSecurityScmSecurityContext();
    }

    @Bean
    @ConditionalOnMissingBean
    public ScmOwnershipGuard scmOwnershipGuard(ScmSecurityContext securityContext) {
        return new ScmOwnershipGuard(securityContext);
    }
}

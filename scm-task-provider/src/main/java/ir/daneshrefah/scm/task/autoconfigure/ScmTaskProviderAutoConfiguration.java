package ir.daneshrefah.scm.task.autoconfigure;

import ir.daneshrefah.scm.common.event.ScmEventPublisher;
import ir.daneshrefah.scm.common.event.SpringScmEventPublisher;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@AutoConfiguration(afterName = "ir.daneshrefah.scm.uaa.client.autoconfigure.ScmResourceServerAutoConfiguration")
@ComponentScan(basePackages = {
        "ir.daneshrefah.scm.task.api",
        "ir.daneshrefah.scm.task.event",
        "ir.daneshrefah.scm.task.mapper",
        "ir.daneshrefah.scm.task.service"
})
@EntityScan(basePackages = "ir.daneshrefah.scm.task.entity")
@EnableJpaRepositories(basePackages = "ir.daneshrefah.scm.task.repository")
public class ScmTaskProviderAutoConfiguration {

    // TODO Replace focused provider scanning with explicit bean registration as the provider surface stabilizes.
    @Bean
    @ConditionalOnMissingBean(ScmEventPublisher.class)
    public ScmEventPublisher scmTaskProviderEventPublisher(ApplicationEventPublisher publisher) {
        return new SpringScmEventPublisher(publisher);
    }
}

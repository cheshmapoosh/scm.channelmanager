package ir.daneshrefah.scm.uaa.client.autoconfigure;

import ir.daneshrefah.scm.observation.ScmObservation;
import ir.daneshrefah.scm.uaa.client.observation.ScmSecurityObservationSupport;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = ScmResourceServerAutoConfiguration.class)
@ConditionalOnBean(ScmObservation.class)
public class ScmSecurityObservationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ScmSecurityObservationSupport scmSecurityObservationSupport(ScmObservation observation) {
        return new ScmSecurityObservationSupport(observation);
    }
}

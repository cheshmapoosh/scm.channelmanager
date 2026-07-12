package ir.daneshrefah.scm.observation.servlet.autoconfigure;

import ir.daneshrefah.scm.observation.servlet.ServletObservationProperties;
import ir.daneshrefah.scm.observation.starter.ObservationContext;
import ir.daneshrefah.scm.observation.starter.ScmObservation;
import ir.daneshrefah.scm.observation.starter.autoconfigure.ScmObservationAutoConfiguration;
import ir.daneshrefah.scm.observation.starter.policy.ObservationSignalPolicy;
import ir.daneshrefah.scm.observation.starter.web.HttpServerObservationFilter;
import ir.daneshrefah.scm.observation.starter.web.ObservationMdcFilter;
import jakarta.servlet.Filter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@AutoConfiguration(after = ScmObservationAutoConfiguration.class)
@ConditionalOnClass(Filter.class)
@EnableConfigurationProperties(ServletObservationProperties.class)
public class ScmServletObservationAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(
            prefix = "scm.observation",
            name = {"enabled", "log.enabled"},
            havingValue = "true"
    )
    static class LogMdcConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "observationMdcFilterRegistration")
        public FilterRegistrationBean<ObservationMdcFilter> observationMdcFilterRegistration(
                ObservationSignalPolicy signalPolicy,
                ObservationContext context
        ) {
            FilterRegistrationBean<ObservationMdcFilter> registration = new FilterRegistrationBean<>();
            registration.setFilter(new ObservationMdcFilter(signalPolicy, context));
            registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 20);
            registration.addUrlPatterns("/*");
            return registration;
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(
            prefix = "scm.observation",
            name = {"enabled", "trace.enabled"},
            havingValue = "true"
    )
    static class HttpServerTraceConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "httpServerObservationFilterRegistration")
        public FilterRegistrationBean<HttpServerObservationFilter> httpServerObservationFilterRegistration(
                ScmObservation observation,
                ObservationSignalPolicy signalPolicy,
                ServletObservationProperties properties
        ) {
            ServletObservationProperties.ServerProperties server = properties.getServer();
            FilterRegistrationBean<HttpServerObservationFilter> registration = new FilterRegistrationBean<>();
            registration.setFilter(new HttpServerObservationFilter(
                    observation,
                    signalPolicy,
                    server.getSpanName()
            ));
            registration.setEnabled(server.isEnabled() && !"channel-only".equalsIgnoreCase(server.getMode()));
            registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 30);
            registration.addUrlPatterns("/*");
            return registration;
        }
    }
}

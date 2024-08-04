package ir.daneshrefah.scm.uaa.config;

import ir.daneshrefah.scm.uaa.filter.CorrelationIdPreProcessingFilter;
import ir.daneshrefah.scm.uaa.filter.RequestLoggingFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-29
 */
@Configuration
public class GlobalConfig {

    @Bean
    public FilterRegistrationBean<CorrelationIdPreProcessingFilter> correlationIdPreProcessingFilter() {
        FilterRegistrationBean<CorrelationIdPreProcessingFilter> registration = new FilterRegistrationBean<CorrelationIdPreProcessingFilter>();
        registration.setFilter(new CorrelationIdPreProcessingFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<RequestLoggingFilter> requestLoggingFilter() {
        FilterRegistrationBean<RequestLoggingFilter> registration = new FilterRegistrationBean<RequestLoggingFilter>();
        registration.setFilter(new RequestLoggingFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }


}

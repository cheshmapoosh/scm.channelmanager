package ir.daneshrefah.scm.core.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.core.serializer.ScmObjectModule;
import ir.daneshrefah.scm.plugin.api.utils.ClassLoader;
import org.apache.camel.model.Resilience4jConfigurationDefinition;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-07
 */
@Configuration
public class ApplicationConfig implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    private static ObjectMapper objectMapper;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        ClassLoader.setApplicationContext(applicationContext);
    }

    private synchronized static void initObjectMapper(ServiceService service) {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new ScmObjectModule(service));
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//        objectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }
    /*@Bean
    public ObjectMapper objectMapper(ServiceService service) {
        if (null == objectMapper)
            initObjectMapper(service);
        return objectMapper;
    }*/

    @Bean
    public LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
    }

    public static ObjectMapper getObjectMapperInstance() {
        return objectMapper;
    }

    @Bean("defaultBreaker")
    public Resilience4jConfigurationDefinition defaultBreaker() {
        Resilience4jConfigurationDefinition config = new Resilience4jConfigurationDefinition();
        config.setFailureRateThreshold("50");
        config.setWaitDurationInOpenState("10");
        config.setMinimumNumberOfCalls("3");
        config.setSlidingWindowSize("5");
        config.setTimeoutEnabled("true");
        config.setTimeoutDuration("3000");
        return config;
    }
}

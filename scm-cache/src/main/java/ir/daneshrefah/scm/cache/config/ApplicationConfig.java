package ir.daneshrefah.scm.cache.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    @Bean
    public ObjectMapper objectMapper(){
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        OBJECT_MAPPER.registerModule(javaTimeModule);
        OBJECT_MAPPER.findAndRegisterModules();
        return OBJECT_MAPPER;
    }
}

package ir.daneshrefah.scm.common.data.audit.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class AuditConfig {

    private static final ObjectMapper OBJECT_MAPPER;
    private final ApplicationContext applicationContext;
    private static ApplicationContext APPLICATION_CONTEXT ;

    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        OBJECT_MAPPER.setDateFormat(new StdDateFormat().withColonInTimeZone(true));
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @PostConstruct
    public void init() {
        AuditConfig.APPLICATION_CONTEXT = applicationContext;
    }

    public static ObjectMapper getObjectMapper(){
        return OBJECT_MAPPER;
    }

    public static ApplicationContext getApplicationContext() {
        return APPLICATION_CONTEXT;
    }
}

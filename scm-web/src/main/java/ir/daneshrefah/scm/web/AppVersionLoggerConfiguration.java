package ir.daneshrefah.scm.web;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
@Slf4j
@Order
public class AppVersionLoggerConfiguration {

    @Value("${scm.application.version:null}")
    private String version;

    @Value("${scm.application.build:null}")
    private String build;


    @Bean
    public CommandLineRunner logCommandLineRunner() {
        return (args) -> {
            String appVersionLog = """
                    
                    ==================================================
                    APP BUILD NUMBER: {}-b{}
                    ==================================================
                    """;
            log.info(appVersionLog,version,build);
        };
    }

}

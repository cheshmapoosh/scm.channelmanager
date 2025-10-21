package ir.daneshrefah.scm.web;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.SpringVersion;
import org.springframework.core.annotation.Order;

import java.time.LocalDateTime;

@Configuration
@Slf4j
@Order
public class AppVersionLoggerConfiguration {

    @Value("${scm.application.version:null}")
    private String version;

    @Value("${scm.application.build:null}")
    private String build;

    @Value("${java.version}")
    private String javaVersion;

    //    @Value("${spring-boot.version}")
//    private String springVersion;
    @Value("${spring.profiles.active}")

    private String profileActive;
    @Value("${spring.application.name}")

    private String applicationName;
    @Value("${scm.channels}")

    private String channel;

    @Value("${server.port}")
    private String serverPort;

    @Value("${server.servlet.context-path}")
    private String contextPath;


    @Bean
    public CommandLineRunner logCommandLineRunner() {
        return (args) -> {
            String appVersionLog = """
                    
                    ==================================================
                               - APPLICATION INFORMATION -
                    
                        Application Version : {}
                        Build Number        : {}
                        Java Version        : {}
                        Spring Boot Version : {}
                        Active Profile      : {}
                        Application Name    : {}
                        Channels            : {}
                        Server Port         : {}
                        Context Path        : {}
                        Start Time          : {}
                    ==================================================
                    """;
            log.info(appVersionLog, version, build, javaVersion, SpringVersion.getVersion(), profileActive, applicationName, channel, serverPort, contextPath, LocalDateTime.now());
        };
    }

}
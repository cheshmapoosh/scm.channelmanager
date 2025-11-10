package ir.daneshrefah.scm.plugin.camel;

import ir.daneshrefah.scm.plugin.camel.config.NabProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(NabProperties.class)
public class CamelWebclientApplication {
    public static void main(String[] args) {
        SpringApplication.run(CamelWebclientApplication.class, args);
    }
}

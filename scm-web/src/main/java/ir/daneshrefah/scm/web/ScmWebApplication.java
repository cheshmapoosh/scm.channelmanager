package ir.daneshrefah.scm.web;

import ir.daneshrefah.scm.docs.client.autoconfigure.ScmDocsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = "ir.daneshrefah.scm")
@EnableConfigurationProperties(ScmDocsProperties.class)
public class ScmWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(ScmWebApplication.class, args);
    }
}

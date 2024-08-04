package ir.daneshrefah.scm.log;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "ir.daneshrefah.scm")
@EntityScan(basePackages = "ir.daneshrefah.scm")
@ComponentScan(basePackages = "ir.daneshrefah.scm")
public class LoggingServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(LoggingServerApplication.class, args);
    }
}

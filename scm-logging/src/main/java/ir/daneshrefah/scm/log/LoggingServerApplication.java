package ir.daneshrefah.scm.log;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "ir.daneshrefah.scm.log")
@EnableJpaRepositories(basePackages = "ir.daneshrefah.scm.common.data.repository.logging")
@EntityScan(basePackages = "ir.daneshrefah.scm.common.data.entity.logging")
public class LoggingServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(LoggingServerApplication.class, args);
    }
}

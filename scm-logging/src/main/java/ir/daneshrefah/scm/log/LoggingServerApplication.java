package ir.daneshrefah.scm.log;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(scanBasePackages = {"ir.daneshrefah"})
@ComponentScan(basePackages = "ir.daneshrefah.scm")
public class LoggingServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(LoggingServerApplication.class, args);
    }
}

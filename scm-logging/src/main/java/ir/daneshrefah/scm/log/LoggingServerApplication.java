package ir.daneshrefah.scm.log;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = "ir.daneshrefah.scm")
@SpringBootApplication(
        scanBasePackages = {"ir.daneshrefah"},
        exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
public class LoggingServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(LoggingServerApplication.class, args);
    }
}

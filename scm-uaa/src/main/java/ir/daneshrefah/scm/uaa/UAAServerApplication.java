package ir.daneshrefah.scm.uaa;

import ir.daneshrefah.scm.uaa.config.DataSourceConfigProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-11-18
 */
@SpringBootApplication
@EnableCaching
@EnableJpaRepositories
@EnableConfigurationProperties(DataSourceConfigProperties.class)
@ComponentScan(basePackages = "ir.daneshrefah.scm")
public class UAAServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(UAAServerApplication.class, args);
    }
}


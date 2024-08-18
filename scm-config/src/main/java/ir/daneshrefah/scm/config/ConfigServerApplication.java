package ir.daneshrefah.scm.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-11-18
 */
@SpringBootApplication(scanBasePackages = {"ir.daneshrefah.scm"})
@EnableConfigServer
@EnableCaching
@EnableJpaAuditing
public class ConfigServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}


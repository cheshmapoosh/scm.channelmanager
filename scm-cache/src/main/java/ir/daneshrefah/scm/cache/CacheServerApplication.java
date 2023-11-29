package ir.daneshrefah.scm.cache;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
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
public class CacheServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(CacheServerApplication.class, args);
    }
}


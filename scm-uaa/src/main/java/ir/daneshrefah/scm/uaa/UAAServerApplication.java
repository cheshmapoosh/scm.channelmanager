package ir.daneshrefah.scm.uaa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-11-18
 */
@SpringBootApplication
@EnableCaching
@ComponentScan(basePackages = "ir.daneshrefah.scm")
public class UAAServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(UAAServerApplication.class, args);
    }
}


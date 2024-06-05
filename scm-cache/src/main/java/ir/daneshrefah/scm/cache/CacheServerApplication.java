package ir.daneshrefah.scm.cache;

//import ir.daneshrefah.scm.cache.client.config.CacheClientAutoConfiguration;
import ir.daneshrefah.scm.cache.client.config.CacheClientAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-11-18
 */
@SpringBootApplication
@EnableCaching
@ComponentScan(basePackages = "ir.daneshrefah.scm", excludeFilters={@ComponentScan.Filter(type= FilterType.ASSIGNABLE_TYPE, value= CacheClientAutoConfiguration.class)})
public class CacheServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(CacheServerApplication.class, args);
    }
}


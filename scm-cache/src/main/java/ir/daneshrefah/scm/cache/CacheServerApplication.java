package ir.daneshrefah.scm.cache;

import ir.daneshrefah.scm.cache.observation.ScmCacheInitLogging;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Starts the SCM Cache Hazelcast member and its Actuator management endpoints.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-11-18
 */
@SpringBootApplication
@EnableScheduling
public class CacheServerApplication {

    public static void main(String[] args) {
        SpringApplication application =
                new SpringApplication(CacheServerApplication.class);

        application.setBannerMode(Banner.Mode.OFF);
        application.addInitializers(new ScmCacheInitLogging());
        application.run(args);
    }
}

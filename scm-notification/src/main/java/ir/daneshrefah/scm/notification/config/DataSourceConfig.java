package ir.daneshrefah.scm.notification.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-07
 */
@Configuration
@ConditionalOnProperty(name = "scm.notification.enabled", havingValue = "true", matchIfMissing = false)
public class DataSourceConfig {
}

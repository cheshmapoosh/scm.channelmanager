package ir.daneshrefah.scm.plugin.api.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({MembershipConfigProperty.class})
public class SpringConfiguration {
}

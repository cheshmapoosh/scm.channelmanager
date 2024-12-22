package ir.daneshrefah.scm.uaa.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "uaaAuditorProvider")
public class JpaConfiguration {
}

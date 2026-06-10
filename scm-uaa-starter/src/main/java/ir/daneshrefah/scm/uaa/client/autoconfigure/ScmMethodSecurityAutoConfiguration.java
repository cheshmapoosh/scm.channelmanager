package ir.daneshrefah.scm.uaa.client.autoconfigure;

import ir.daneshrefah.scm.uaa.client.properties.ScmResourceServerProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@AutoConfiguration
@ConditionalOnClass(EnableMethodSecurity.class)
@ConditionalOnProperty(
        prefix = "scm.security.resource-server",
        name = "method-security-enabled",
        havingValue = "true",
        matchIfMissing = true
)
@EnableConfigurationProperties(ScmResourceServerProperties.class)
@EnableMethodSecurity
public class ScmMethodSecurityAutoConfiguration {
}

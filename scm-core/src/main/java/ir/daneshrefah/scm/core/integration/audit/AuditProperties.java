package ir.daneshrefah.scm.core.integration.audit;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "scm.audit")
public class AuditProperties {
    private boolean enabled = true;
    private String outputPath = "log/scm/scm-web/audit.ndjson";
}

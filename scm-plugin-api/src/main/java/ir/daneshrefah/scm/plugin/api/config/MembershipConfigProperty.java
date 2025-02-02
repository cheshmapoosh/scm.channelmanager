package ir.daneshrefah.scm.plugin.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "scm.membership-access")
@Data
public class MembershipConfigProperty {
    private List<String> defaultServices;
}

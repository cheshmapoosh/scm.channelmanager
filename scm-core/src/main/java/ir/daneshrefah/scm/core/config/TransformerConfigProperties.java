package ir.daneshrefah.scm.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "scm.transformers")
@Component
public class TransformerConfigProperties {
    private String dir;
}
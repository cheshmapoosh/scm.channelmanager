package ir.daneshrefah.scm.plugin.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties(prefix = "scm.asset-provider")
@Getter
@Setter
public class AssetProviderConfigProperties {

    private Map<String,AssetProviderConfig> configs;

    @Getter
    @Setter
    public static class AssetProviderConfig {
        private String providerServiceCode;
    }
}

package ir.daneshrefah.scm.core.integration.gateway.contract;

import ir.daneshrefah.scm.common.model.protocol.ProtocolType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "scm.gateway.client-contracts")
@Getter
@Setter
public class ScmGatewayClientContractProperties {
    private Map<ProtocolType, ProtocolDefaults> defaults = new EnumMap<>(ProtocolType.class);

    public void setDefaults(Map<ProtocolType, ProtocolDefaults> defaults) {
        this.defaults = new EnumMap<>(ProtocolType.class);
        if (defaults != null) {
            this.defaults.putAll(defaults);
        }
    }

    @Getter
    @Setter
    public static class ProtocolDefaults {
        private ContractDefinition defaultContract;
        private Map<String, ContractDefinition> versions = new LinkedHashMap<>();

        public ContractDefinition getDefault() {
            return defaultContract;
        }

        public void setDefault(ContractDefinition defaultContract) {
            this.defaultContract = defaultContract;
        }

        public void setVersions(Map<String, ContractDefinition> versions) {
            this.versions = versions == null ? new LinkedHashMap<>() : new LinkedHashMap<>(versions);
        }
    }

    @Getter
    @Setter
    public static class ContractDefinition {
        private String name;
        private String requestDecoder;
        private String responseEncoder;
        private String faultEncoder;
    }
}

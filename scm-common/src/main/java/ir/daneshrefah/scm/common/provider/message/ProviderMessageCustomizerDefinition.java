package ir.daneshrefah.scm.common.provider.message;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
public class ProviderMessageCustomizerDefinition {
    private String type;
    private Integer order;
    private Map<String, Object> config = new LinkedHashMap<>();

    public Map<String, Object> config() {
        return config == null ? Map.of() : Map.copyOf(config);
    }
}

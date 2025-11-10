package ir.daneshrefah.scm.plugin.camel.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Reads values from nab.properties automatically.
 */
@Component
@ConfigurationProperties(prefix = "scm.atps")
public class NabProperties {


    private Map<String, String> values = new HashMap<>();

    public Map<String, String> getValues() {
        return values;
    }

    public void setValues(Map<String, String> values) {
        this.values = values;
    }

    public String get(String key) {
        return values.get(key);
    }

    public String getOrDefault(String key, String def) {
        return values.getOrDefault(key, def);
    }

    public String getUsername() {
        return values.getOrDefault("username", "999998");
    }

    public String getPassword() {
        return values.getOrDefault("password", "1234567890");
    }

    public String getDefaultTerminalType() {
        return values.getOrDefault("default.terminal.type", "99");
    }
}

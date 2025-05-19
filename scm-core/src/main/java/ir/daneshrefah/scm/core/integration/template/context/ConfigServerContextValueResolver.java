package ir.daneshrefah.scm.core.integration.template.context;

import org.apache.camel.Exchange;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class ConfigServerContextValueResolver implements ContextValueResolver {
    private final Environment env;

    public ConfigServerContextValueResolver(Environment env) {
        this.env = env;
    }

    @Override
    public boolean supports(String key) {
        return key.startsWith("config.");
    }

    @Override
    public Object resolve(String key, Exchange exchange) {
        String property = key.substring("config.".length());
        return env.getProperty(property);
    }
}
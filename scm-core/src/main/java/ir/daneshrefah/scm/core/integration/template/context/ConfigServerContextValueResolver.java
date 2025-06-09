package ir.daneshrefah.scm.core.integration.template.context;

import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
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
        return StringUtils.startsWithIgnoreCase(key, "config.");
    }

    @Override
    public Object resolve(String key, Exchange exchange) {
        String property = StringUtils.removeStartIgnoreCase(key, "config.");
        return env.getProperty(property);
    }
}
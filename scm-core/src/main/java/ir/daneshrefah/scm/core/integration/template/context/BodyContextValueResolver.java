package ir.daneshrefah.scm.core.integration.template.context;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class BodyContextValueResolver implements ContextValueResolver {
    @Override
    public boolean supports(String key) {
        return StringUtils.startsWithIgnoreCase(key, "body.");
    }

    @Override
    public Object resolve(String key, Exchange exchange) {
        String name = StringUtils.removeStartIgnoreCase(key, "body.");
        JsonNode body = exchange.getIn().getBody(JsonNode.class);
        JsonNode value = body.path(name);
        if (value == null || value.isNull() || value.isMissingNode()) {
            return null;
        }
        return value.asText();
    }
}
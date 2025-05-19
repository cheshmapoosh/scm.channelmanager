package ir.daneshrefah.scm.core.integration.template.context;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

@Component
public class BodyContextValueResolver implements ContextValueResolver {
    @Override
    public boolean supports(String key) {
        return key.startsWith("body.");
    }

    @Override
    public Object resolve(String key, Exchange exchange) {
        String name = key.substring("body.".length());
        JsonNode body = exchange.getIn().getBody(JsonNode.class);
        JsonNode value = body.path(name);
        if (value == null || value.isNull() || value.isMissingNode()) {
            return null;
        }
        return value.asText();
    }
}
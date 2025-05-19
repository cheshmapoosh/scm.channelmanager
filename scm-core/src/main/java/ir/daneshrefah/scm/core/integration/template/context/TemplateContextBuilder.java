package ir.daneshrefah.scm.core.integration.template.context;

import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class TemplateContextBuilder {
    private final List<ContextValueResolver> resolvers;

    public Map<String, Object> buildContext(Set<String> variables, Exchange exchange) {
        Map<String, Object> context = new HashMap<>();
        for (String key : variables) {
            for (ContextValueResolver resolver : resolvers) {
                if (resolver.supports(key)) {
                    Object value = resolver.resolve(key, exchange);
                    context.put(key, value);
                    break;
                }
            }
        }
        return buildNestedContext(context);
    }

    private Map<String, Object> buildNestedContext(Map<String, Object> flatMap) {
        Map<String, Object> result = new HashMap<>();

        for (Map.Entry<String, Object> entry : flatMap.entrySet()) {
            String[] keys = entry.getKey().split("\\.");
            Map<String, Object> current = result;

            for (int i = 0; i < keys.length - 1; i++) {
                current = (Map<String, Object>) current.computeIfAbsent(keys[i], k -> new HashMap<>());
            }

            current.put(keys[keys.length - 1], entry.getValue());
        }

        return result;
    }
}

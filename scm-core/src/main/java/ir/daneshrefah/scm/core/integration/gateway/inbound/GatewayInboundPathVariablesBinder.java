package ir.daneshrefah.scm.core.integration.gateway.inbound;

import ir.daneshrefah.scm.common.model.gateway.InboundChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.message.Message;
import org.apache.camel.model.RouteDefinition;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class GatewayInboundPathVariablesBinder {
    private static final Pattern PATH_VARIABLE = Pattern.compile("\\{([^{}]+)}");

    public void bind(RouteDefinition route, InboundChannelServiceDefinition definition) {
        Set<String> variableNames = variableNames(definition == null ? null : definition.getPath());
        route.process(exchange -> {
            if (variableNames.isEmpty()) {
                exchange.setProperty(Message.INBOUND_PATH_VARIABLES, Map.of());
                return;
            }
            Map<String, Object> variables = new LinkedHashMap<>();
            for (String variableName : variableNames) {
                Object value = exchange.getMessage().getHeader(variableName);
                if (value != null) {
                    variables.put(variableName, value);
                }
            }
            exchange.setProperty(
                    Message.INBOUND_PATH_VARIABLES,
                    Collections.unmodifiableMap(variables)
            );
        });
    }

    private Set<String> variableNames(String path) {
        if (path == null || path.isBlank()) {
            return Set.of();
        }
        Set<String> variableNames = new LinkedHashSet<>();
        Matcher matcher = PATH_VARIABLE.matcher(path);
        while (matcher.find()) {
            String variableName = matcher.group(1).trim();
            if (!variableName.isEmpty()) {
                variableNames.add(variableName);
            }
        }
        return Set.copyOf(variableNames);
    }
}

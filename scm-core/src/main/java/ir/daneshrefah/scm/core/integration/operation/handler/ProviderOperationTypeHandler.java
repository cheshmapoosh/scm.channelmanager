package ir.daneshrefah.scm.core.integration.operation.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.operation.Operation;
import ir.daneshrefah.scm.common.model.operation.OperationType;
import ir.daneshrefah.scm.common.model.message.Message;
import lombok.RequiredArgsConstructor;
import org.apache.camel.model.RouteDefinition;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ProviderOperationTypeHandler implements OperationTypeHandler {

    public static final String OPERATION_PROVIDER_NAME = "scmOperationProviderName";
    public static final String OPERATION_PROVIDER_URI = "scmOperationProviderUri";
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;

    @Override
    public OperationType getOperationType() {
        return OperationType.PROVIDER;
    }

    @Override
    public void config(RouteDefinition route, Operation operation) {
        validateProvider(operation);
        String targetUri = resolveTargetUri(operation);
        route.process(exchange -> {
            exchange.getMessage().setHeader(OPERATION_PROVIDER_NAME, operation.getProvider().getName());
            exchange.getMessage().setHeader(OPERATION_PROVIDER_URI, targetUri);
            Object body = exchange.getMessage().getBody();
            if (body instanceof Message message) {
                body = message.getPayload();
            }
            exchange.getMessage().setBody(toMap(body));
        });

        route.to(targetUri);
    }

    private void validateProvider(Operation operation) {
        if (operation.getProvider() == null) {
            throw new IllegalArgumentException("Provider operation must be bound to a provider instance: " + operation.getName());
        }
        if (!Boolean.TRUE.equals(operation.getProvider().getActive())) {
            throw new IllegalArgumentException("Provider operation is bound to an inactive provider: " + operation.getName());
        }
    }

    private Map<String, Object> toMap(Object body) {
        switch (body) {
            case null -> {
                return Map.of();
            }
            case Map<?, ?> map -> {
                return objectMapper.convertValue(map, MAP_TYPE);
            }
            case String text -> {
                if (StringUtils.isBlank(text)) {
                    return Map.of();
                }
                try {
                    return objectMapper.readValue(text, MAP_TYPE);
                } catch (Exception e) {
                    throw new IllegalArgumentException("Provider operation body must be a JSON object", e);
                }
            }
            default -> {
            }
        }
        return objectMapper.convertValue(body, MAP_TYPE);
    }

    private String resolveTargetUri(Operation operation) {
        String uri = StringUtils.trimToNull(operation.getProvider().getUri());
        if (uri == null) {
            throw new IllegalArgumentException("Provider operation target URI is empty for operation " + operation.getName());
        }
        if (!StringUtils.contains(uri, ':')) {
            throw new IllegalArgumentException("Provider operation target URI must include a Camel scheme for operation " + operation.getName());
        }
        if (uri.endsWith(":")) {
            return uri + operation.getName();
        }
        return uri;
    }
}

package ir.daneshrefah.scm.core.integration.service.routing;

import ir.daneshrefah.scm.common.model.operation.Operation;
import ir.daneshrefah.scm.common.model.operation.OperationType;
import ir.daneshrefah.scm.common.service.operation.OperationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@RequiredArgsConstructor
public class RoutingOperationMetadataResolver {
    private final OperationService operationService;
    private final ConcurrentMap<String, String> spanKinds = new ConcurrentHashMap<>();

    public String spanKind(String operationName) {
        if (operationName == null || operationName.isBlank()) {
            throw new IllegalArgumentException("operationName is required for routing metadata");
        }
        return spanKinds.computeIfAbsent(
                operationName.trim().toLowerCase(Locale.ROOT),
                ignored -> loadSpanKind(operationName.trim())
        );
    }

    private String loadSpanKind(String operationName) {
        List<Operation> operations = operationService.findActiveOperationsByNames(
                List.of(operationName)
        );
        Operation operation = operations.stream()
                .filter(candidate -> candidate != null
                        && candidate.getName() != null
                        && candidate.getName().equalsIgnoreCase(operationName))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Active operation metadata not found for operationName=" + operationName));
        return operation.getType() == OperationType.PROVIDER
                || operation.getType() == OperationType.REST
                ? "client"
                : "internal";
    }
}

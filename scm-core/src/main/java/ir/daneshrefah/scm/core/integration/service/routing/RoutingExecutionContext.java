package ir.daneshrefah.scm.core.integration.service.routing;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class RoutingExecutionContext {
    private final Object originalRequest;
    private Long processId;
    private String correlationId;
    private Object transactionData;
    private final Map<String, Object> stepResults = new LinkedHashMap<>();

    public RoutingExecutionContext(Object originalRequest) { this.originalRequest = originalRequest; }
    public RoutingExecutionContext(
            Object originalRequest,
            Long processId,
            String correlationId,
            Object transactionData,
            Map<String, Object> stepResults
    ) {
        this.originalRequest = originalRequest;
        this.processId = processId;
        this.correlationId = correlationId;
        this.transactionData = transactionData;
        if (stepResults != null) {
            this.stepResults.putAll(stepResults);
        }
    }
    public Object originalRequest() { return originalRequest; }
    public Long processId() { return processId; }
    public void processId(Long processId) { this.processId = processId; }
    public String correlationId() { return correlationId; }
    public void correlationId(String correlationId) { this.correlationId = correlationId; }
    public Object transactionData() { return transactionData; }
    public void transactionData(Object transactionData) { this.transactionData = transactionData; }
    public Map<String, Object> stepResults() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(stepResults));
    }
    public void record(String stepId, Object response) { stepResults.put(stepId, response); }
    public Object result(String stepId) { return stepResults.get(stepId); }
}

package ir.daneshrefah.scm.core.integration.service.routing;

public record RoutingStepObservationContext(
        String serviceCode,
        String inboundAction,
        String taskRole,
        int stepIndex,
        String spanKind
) {
    public RoutingStepObservationContext(
            String serviceCode,
            String inboundAction,
            String taskRole,
            int stepIndex
    ) {
        this(serviceCode, inboundAction, taskRole, stepIndex, "internal");
    }
}

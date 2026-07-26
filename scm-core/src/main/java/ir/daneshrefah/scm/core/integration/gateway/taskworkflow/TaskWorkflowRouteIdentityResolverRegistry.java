package ir.daneshrefah.scm.core.integration.gateway.taskworkflow;

import ir.daneshrefah.scm.common.model.protocol.ProtocolType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class TaskWorkflowRouteIdentityResolverRegistry {
    private final Map<ProtocolType, TaskWorkflowRouteIdentityResolver> resolvers;

    public TaskWorkflowRouteIdentityResolverRegistry(
            List<TaskWorkflowRouteIdentityResolver> resolvers
    ) {
        EnumMap<ProtocolType, TaskWorkflowRouteIdentityResolver> indexed =
                new EnumMap<>(ProtocolType.class);
        for (TaskWorkflowRouteIdentityResolver resolver : resolvers) {
            for (ProtocolType protocol : ProtocolType.values()) {
                if (!resolver.supports(protocol)) {
                    continue;
                }
                if (indexed.putIfAbsent(protocol, resolver) != null) {
                    throw new IllegalStateException(
                            "Duplicate TASK_WORKFLOW route identity resolver for protocol="
                                    + protocol);
                }
            }
        }
        this.resolvers = Map.copyOf(indexed);
    }

    public TaskWorkflowRouteIdentityResolver getRequired(ProtocolType protocol) {
        TaskWorkflowRouteIdentityResolver resolver = resolvers.get(protocol);
        if (resolver == null) {
            throw new TaskWorkflowRouteIdentityException(
                    "No TASK_WORKFLOW route identity resolver for protocol=" + protocol);
        }
        return resolver;
    }
}

package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class RoutingRecoveryPolicyRegistry {
    private final Map<RoutingStrategy, RoutingRecoveryPolicy> policies;

    public RoutingRecoveryPolicyRegistry(List<RoutingRecoveryPolicy> policies) {
        EnumMap<RoutingStrategy, RoutingRecoveryPolicy> indexed =
                new EnumMap<>(RoutingStrategy.class);
        for (RoutingRecoveryPolicy policy : policies) {
            if (indexed.putIfAbsent(policy.strategy(), policy) != null) {
                throw new IllegalStateException(
                        "Duplicate routing recovery policy for strategy="
                                + policy.strategy());
            }
        }
        this.policies = Map.copyOf(indexed);
    }

    public RoutingRecoveryPolicy getRequired(RoutingStrategy strategy) {
        RoutingRecoveryPolicy policy = policies.get(strategy);
        if (policy == null) {
            throw new IllegalStateException(
                    "No routing recovery policy for strategy=" + strategy);
        }
        return policy;
    }
}

package ir.daneshrefah.scm.core.integration.service.routing;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class RoutingDecisionPolicyRegistry {
    private final Map<String, RoutingDecisionPolicy> policies;

    public RoutingDecisionPolicyRegistry(List<RoutingDecisionPolicy> policies) {
        Map<String, RoutingDecisionPolicy> indexed = new LinkedHashMap<>();
        for (RoutingDecisionPolicy policy : policies) {
            String code = normalize(policy.code());
            if (indexed.putIfAbsent(code, policy) != null) {
                throw new IllegalStateException(
                        "Duplicate routing decision policy code=" + code);
            }
        }
        this.policies = Map.copyOf(indexed);
    }

    public RoutingDecisionPolicy getRequired(String code) {
        RoutingDecisionPolicy policy = policies.get(normalize(code));
        if (policy == null) {
            throw new IllegalStateException(
                    "Unsupported routing decision policy code=" + code);
        }
        return policy;
    }

    private String normalize(String code) {
        String value = StringUtils.trimToNull(code);
        if (value == null) {
            throw new IllegalArgumentException(
                    "Routing decision policy code must not be blank");
        }
        return value.toUpperCase(Locale.ROOT);
    }
}

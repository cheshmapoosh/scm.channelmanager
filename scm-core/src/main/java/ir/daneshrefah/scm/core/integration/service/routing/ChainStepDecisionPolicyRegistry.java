package ir.daneshrefah.scm.core.integration.service.routing;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class ChainStepDecisionPolicyRegistry {
    private final Map<String, ChainStepDecisionPolicy> policies;

    public ChainStepDecisionPolicyRegistry(List<ChainStepDecisionPolicy> policies) {
        Map<String, ChainStepDecisionPolicy> indexed = new LinkedHashMap<>();
        for (ChainStepDecisionPolicy policy : policies) {
            String code = normalize(policy.code());
            if (indexed.putIfAbsent(code, policy) != null) {
                throw new IllegalStateException("Duplicate chain decision policy code=" + code);
            }
        }
        this.policies = Map.copyOf(indexed);
    }

    public ChainStepDecisionPolicy getRequired(String code) {
        ChainStepDecisionPolicy policy = policies.get(normalize(code));
        if (policy == null) {
            throw new IllegalStateException("Unsupported chain decision policy code=" + code);
        }
        return policy;
    }

    private String normalize(String code) {
        String value = StringUtils.trimToNull(code);
        if (value == null) {
            throw new IllegalArgumentException("Chain decision policy code must not be blank");
        }
        return value.toUpperCase(Locale.ROOT);
    }
}

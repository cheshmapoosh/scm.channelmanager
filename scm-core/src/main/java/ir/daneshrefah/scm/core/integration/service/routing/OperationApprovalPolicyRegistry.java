package ir.daneshrefah.scm.core.integration.service.routing;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class OperationApprovalPolicyRegistry {
    private final Map<String, OperationApprovalPolicy> policies;

    public OperationApprovalPolicyRegistry(List<OperationApprovalPolicy> policies) {
        Map<String, OperationApprovalPolicy> registeredPolicies = new HashMap<>();
        for (OperationApprovalPolicy policy : policies) {
            if (policy == null) {
                throw new IllegalStateException("Operation approval policy must not be null");
            }
            String code = normalizeRequired(policy.code(), "Operation approval policy code must not be blank");
            OperationApprovalPolicy existing = registeredPolicies.putIfAbsent(code, policy);
            if (existing != null) {
                throw new IllegalStateException("Duplicate operation approval policy code: " + code);
            }
        }
        this.policies = Map.copyOf(registeredPolicies);
    }

    public OperationApprovalPolicy getRequired(String code) {
        String normalizedCode = normalizeRequired(code, "Operation approval policy code must not be blank");
        OperationApprovalPolicy policy = policies.get(normalizedCode);
        if (policy == null) {
            throw new IllegalStateException("Unsupported operation approval policy code: " + normalizedCode);
        }
        return policy;
    }

    private String normalizeRequired(String code, String message) {
        if (StringUtils.isBlank(code)) {
            throw new IllegalArgumentException(message);
        }
        return code.trim().toUpperCase(Locale.ROOT);
    }
}

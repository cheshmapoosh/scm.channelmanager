package ir.daneshrefah.scm.cache.infrastructure.hazelcast;

import ir.daneshrefah.scm.observation.element.ScmElementRiskEngine;
import ir.daneshrefah.scm.observation.element.ScmElementRiskPolicy;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HazelcastElementRiskPolicyProvider {
    private final HazelcastElementRiskProperties properties;
    private final ScmElementRiskEngine riskEngine;

    @PostConstruct
    void validateConfiguredPolicies() {
        riskEngine.validate(properties.resolvePolicy(null));
        for (String elementName : properties.getElements().keySet()) {
            riskEngine.validate(properties.resolvePolicy(elementName));
        }
    }

    public ScmElementRiskPolicy policyFor(String elementName) {
        ScmElementRiskPolicy policy = properties.resolvePolicy(elementName);
        riskEngine.validate(policy);
        return policy;
    }
}

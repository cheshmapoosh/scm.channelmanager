package ir.daneshrefah.scm.cache.infrastructure.hazelcast;

import ir.daneshrefah.scm.observation.element.ScmElementRiskEngine;
import ir.daneshrefah.scm.observation.element.ScmElementRiskPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HazelcastElementRiskPolicyProvider {
    private final HazelcastElementRiskProperties properties;
    private final ScmElementRiskEngine riskEngine;

    public ScmElementRiskPolicy policyFor(String elementName) {
        ScmElementRiskPolicy policy = properties.resolvePolicy(elementName);
        riskEngine.validate(policy);
        return policy;
    }
}

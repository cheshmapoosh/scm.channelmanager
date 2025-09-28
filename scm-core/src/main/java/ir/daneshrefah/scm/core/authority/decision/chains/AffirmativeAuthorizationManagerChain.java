package ir.daneshrefah.scm.core.authority.decision.chains;

import ir.daneshrefah.scm.core.authority.decision.configuration.builder.AuthorizationManagerDecisionChainBuilder;
import ir.daneshrefah.scm.core.authority.decision.configuration.handler.AuthorizationManagerDecisionChain;
import org.springframework.stereotype.Component;

@Component
public class AffirmativeAuthorizationManagerChain implements AuthorizationManagerDecisionChain {

    @Override
    public AuthorizationManagerDecisionChainBuilder decisionChain() {
        return AuthorizationManagerDecisionChainBuilder
                .createWithAffirmativeManger()
                .registerDynamically();
    }

}

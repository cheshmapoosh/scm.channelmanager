package ir.daneshrefah.scm.core.authority.decision.chains;

import ir.daneshrefah.scm.core.authority.decision.configuration.builder.AuthorizationManagerDecisionChainBuilder;
import ir.daneshrefah.scm.core.authority.decision.configuration.handler.AuthorizationManagerDecisionChain;
import ir.daneshrefah.scm.core.authority.decision.voter.RoleCheckAuthorizationManager;
import ir.daneshrefah.scm.core.authority.decision.voter.TransactionAuthenticationSecurityProvider;
import org.springframework.stereotype.Component;

@Component
public class DefaultAuthorizationManagerChain implements AuthorizationManagerDecisionChain {

    @Override
    public AuthorizationManagerDecisionChainBuilder decisionChain() {
        return AuthorizationManagerDecisionChainBuilder
                .createWithAffirmativeManger()
                .register(RoleCheckAuthorizationManager.class)
                .register(TransactionAuthenticationSecurityProvider.class);
    }
}
